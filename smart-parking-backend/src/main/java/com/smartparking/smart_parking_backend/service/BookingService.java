package com.smartparking.smart_parking_backend.service;

import com.smartparking.smart_parking_backend.dto.BookingRequestDTO;
import com.smartparking.smart_parking_backend.dto.BookingResponseDTO;
import com.smartparking.smart_parking_backend.exception.ResourceNotFoundException;
import com.smartparking.smart_parking_backend.exception.SlotAlreadyBookedException;
import com.smartparking.smart_parking_backend.model.*;
import com.smartparking.smart_parking_backend.model.PaymentStatus; // Explicit import
import com.smartparking.smart_parking_backend.repository.BookingRepository;
import com.smartparking.smart_parking_backend.repository.ParkingSlotRepository;
import com.smartparking.smart_parking_backend.repository.VehicleRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import com.smartparking.smart_parking_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookingService Class
 * 
 * Purpose: This is the core engine of the application. It handles the entire lifecycle of a parking 
 * reservation: booking a slot, checking capacities, ending the booking, calculating prices, and processing payments.
 * 
 * Key Annotation:
 * - @Service: Registers this class as a Spring Service bean containing business logic.
 */
@Service
public class BookingService {

        // Dependencies needed to talk to the database
        private final BookingRepository bookingRepository;
        private final ParkingSlotRepository slotRepository;
        private final VehicleRepository vehicleRepository;
        private final UserRepository userRepository;

        // Constructor Dependency Injection
        public BookingService(
                        BookingRepository bookingRepository,
                        ParkingSlotRepository slotRepository,
                        VehicleRepository vehicleRepository,
                        UserRepository userRepository) {
                this.bookingRepository = bookingRepository;
                this.slotRepository = slotRepository;
                this.vehicleRepository = vehicleRepository;
                this.userRepository = userRepository;
        }

        // =====================================================
        // BOOK SLOT (AUTOMATED – NO OWNER / ADMIN APPROVAL)
        // =====================================================
        
        /**
         * Creates a new active booking for a user.
         * @Transactional ensures that if any part of this method fails (e.g., slot is full), 
         * all database changes made so far are rolled back to prevent corrupted data.
         */
        @Transactional
        public BookingResponseDTO bookSlot(BookingRequestDTO dto) {
                // 1. 🔐 Get Authenticated user (fetch email from the secure Security Context)
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                
                // Fetch the actual User object from the database using that email
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // 2. 🔒 Lock the parking slot row in the database (prevents race condition)
                // This stops two people from booking the exact same slot at the exact same millisecond.
                ParkingSlot slot = slotRepository.findSlotForUpdate(dto.getSlotId())
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // 3. 🚫 Check if Slot is disabled (maintenance / reserved by owner)
                if (!slot.isEnabled()) {
                        throw new RuntimeException("Slot temporarily unavailable");
                }

                // 4. 🚗 Vehicle Creation (Inline)
                // Create a record of the vehicle being parked
                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleNumber(dto.getVehicleNumber());
                vehicle.setVehicleModel(dto.getVehicleModel());
                vehicle.setVehicleType(dto.getVehicleType());
                vehicle.setOwnerName(dto.getOwnerName()); // Optional
                vehicle.setUser(user);

                // Save the vehicle to the database
                vehicleRepository.save(vehicle);

                VehicleType vehicleType = vehicle.getVehicleType();

                // 5. 📊 Capacity Check (AUTOMATED AVAILABILITY)
                // Ask the database: How many active bookings currently exist for this specific vehicle type at this specific slot?
                long activeCount = bookingRepository.countByParkingSlotAndVehicle_VehicleTypeAndActiveTrue(
                                slot,
                                vehicleType);

                // Determine the maximum capacity allowed for this vehicle type at this location
                int capacity = 0;
                if (vehicleType == VehicleType.CAR) {
                        capacity = slot.getCarCapacity();
                } else if (vehicleType == VehicleType.BIKE) {
                        capacity = slot.getBikeCapacity();
                } else if (vehicleType == VehicleType.TRUCK) {
                        capacity = slot.getTruckCapacity();
                }

                // If the number of active parkers equals or exceeds the capacity, reject the booking
                if (activeCount >= capacity) {
                        throw new SlotAlreadyBookedException(
                                        vehicleType + " slots are fully occupied at this location");
                }

                // 6. 🆕 Create the actual Booking record (ACTIVE immediately upon creation)
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setVehicle(vehicle);
                booking.setParkingSlot(slot);
                booking.setStartTime(LocalDateTime.now()); // The clock starts ticking now
                booking.setActive(true);                   // Mark as currently parked

                Booking saved = bookingRepository.save(booking);

                // 7. 👑 Fetch ADMIN user for Centralized Payment
                // If the slot owner hasn't provided a UPI ID, the payment goes to the Admin.
                User adminUser = userRepository.findByEmail("admin@smartparking.com")
                                .orElseThrow(() -> new RuntimeException("Admin account not configured"));

                // 8. Return all booking details back to the frontend
                return new BookingResponseDTO(
                                saved.getId(),
                                slot.getId(),
                                slot.getLocation(),
                                vehicle.getVehicleNumber(),
                                vehicle.getVehicleModel(),
                                vehicleType,
                                user.getId(),
                                user.getName(),
                                user.getPhone(),
                                slot.getUpiId() != null && !slot.getUpiId().isEmpty() ? slot.getUpiId()
                                                : adminUser.getUpiId(), // 👈 Use Slot/Owner UPI ID, fallback to Admin
                                saved.getStartTime(),
                                saved.getEndTime(),
                                saved.isActive(),
                                0.0, // Initial price is 0 since they just arrived
                                PaymentStatus.PENDING // Payment happens when they leave
                );
        }

        // =====================================================
        // END BOOKING (USER)
        // =====================================================
        
        /**
         * Stops the parking clock and calculates the total price based on duration.
         */
        @Transactional
        public BookingResponseDTO endBooking(Long bookingId) {

                // Find the booking in the database
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                // Prevent ending an already ended booking
                if (!booking.isActive()) {
                        throw new RuntimeException("Booking already ended");
                }

                // Stop the clock and mark as inactive
                booking.setEndTime(LocalDateTime.now());
                booking.setActive(false);

                // ⏱ Calculate duration in minutes for precise billing
                // We use java.time.Duration.between() to get the exact time difference
                long minutes = java.time.Duration
                                .between(booking.getStartTime(), booking.getEndTime())
                                .toMinutes();

                // Minimum 1 minute charge to avoid $0.00 for immediate close operations
                // Even if booked for 1 second, we charge for 1 minute
                if (minutes < 1) {
                        minutes = 1;
                }

                ParkingSlot slot = booking.getParkingSlot();
                VehicleType type = booking.getVehicle().getVehicleType();

                // Select the correct price tier based on the vehicle type parked (CAR, BIKE, TRUCK)
                double pricePerHour = switch (type) {
                        case CAR -> slot.getCarPricePerHour();
                        case BIKE -> slot.getBikePricePerHour();
                        case TRUCK -> slot.getTruckPricePerHour();
                };

                // 🧮 Calculate price: 
                // (minutes / 60.0) gives us hours in decimal (e.g., 90 mins = 1.5 hours)
                // We multiply that by pricePerHour to get the precise final amount
                double totalPrice = (minutes / 60.0) * pricePerHour;
                booking.setTotalPrice(totalPrice);
                
                // Ensure payment status is PENDING if not already paid (future proofing against prepayments)
                if (booking.getPaymentStatus() == null) {
                        booking.setPaymentStatus(PaymentStatus.PENDING);
                }

                // Save the updated, finalized booking to the database
                Booking saved = bookingRepository.save(booking);
                return mapToDTO(saved);
        }

        // =====================================================
        // PROCESS PAYMENT (NEW)
        // =====================================================
        
        /**
         * Marks a completed booking as paid after receiving a transaction reference.
         */
        @Transactional
        public BookingResponseDTO processPayment(Long bookingId, String paymentRef) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                // Ensure the user actually ended their parking session before trying to pay
                if (booking.isActive()) {
                        throw new RuntimeException("Cannot pay for an active booking. End it first.");
                }

                // Prevent double payments
                if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
                        return mapToDTO(booking);
                }

                // Save Manual Payment Reference (e.g., UTR number from a UPI app)
                if (paymentRef != null && !paymentRef.trim().isEmpty()) {
                        booking.setPaymentReference(paymentRef);
                }

                // Mark COMPLETED 
                // Note: In a production app, an admin might need to verify this UTR before treating it as COMPLETED.
                booking.setPaymentStatus(PaymentStatus.COMPLETED);
                
                Booking saved = bookingRepository.save(booking);
                return mapToDTO(saved);
        }

        /**
         * Helper Method: Converts a raw Database Booking object into a simplified BookingResponseDTO 
         * that is safe to send to the frontend (hiding passwords, complex DB relationships, etc.).
         */
        private BookingResponseDTO mapToDTO(Booking booking) {
                return new BookingResponseDTO(
                                booking.getId(),
                                booking.getParkingSlot().getId(),
                                booking.getParkingSlot().getLocation(),
                                booking.getVehicle().getVehicleNumber(),
                                booking.getVehicle().getVehicleModel(),
                                booking.getVehicle().getVehicleType(),
                                booking.getUser().getId(),
                                booking.getUser().getName(),
                                booking.getUser().getPhone(), // Added phone number
                                booking.getParkingSlot().getUpiId() != null
                                                && !booking.getParkingSlot().getUpiId().isEmpty()
                                                                ? booking.getParkingSlot().getUpiId()
                                                                : userRepository.findByEmail("admin@smartparking.com")
                                                                                .map(User::getUpiId)
                                                                                .orElse(null),
                                booking.getStartTime(),
                                booking.getEndTime(),
                                booking.isActive(),
                                booking.getTotalPrice(),
                                booking.getPaymentStatus());
        }

        // =====================================================
        // GET BOOKINGS OF LOGGED-IN USER
        // =====================================================
        
        /**
         * Fetches the booking history for the user who is currently logged in.
         */
        @Transactional(readOnly = true)
        public List<BookingResponseDTO> getMyBookings() {
                // Determine who is requesting this data based on their secure login token
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Fetch bookings from DB, convert each to DTO, and return the list
                return bookingRepository.findByUser(user)
                                .stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        // =====================================================
        // GET BOOKINGS FOR OWNER (INCOMING BOOKINGS)
        // =====================================================
        
        /**
         * Fetches the history of all vehicles that have parked at properties belonging to the logged-in Owner.
         */
        @Transactional(readOnly = true)
        public List<BookingResponseDTO> getOwnerBookings() {
                // Determine who is requesting this list
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                // Basic safety check: ensure the owner exists
                if (!userRepository.existsByEmail(email)) {
                        throw new ResourceNotFoundException("User not found");
                }

                // Fetch bookings linked to any slot this email address owns, convert to DTOs, and return
                return bookingRepository.findByParkingSlot_Owner_Email(email)
                                .stream()
                                .map(this::mapToDTO)
                                .toList();
        }
}
