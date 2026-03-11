package com.smartparking.smart_parking_backend.service;

import com.smartparking.smart_parking_backend.dto.ParkingSlotRequestDTO;
import com.smartparking.smart_parking_backend.dto.ParkingSlotResponseDTO;
import com.smartparking.smart_parking_backend.dto.SlotAvailabilityResponseDTO;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.model.Role;
import com.smartparking.smart_parking_backend.model.ParkingSlot;
import com.smartparking.smart_parking_backend.model.VehicleType;
import com.smartparking.smart_parking_backend.repository.BookingRepository;
import com.smartparking.smart_parking_backend.repository.ParkingSlotRepository;
import com.smartparking.smart_parking_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import com.smartparking.smart_parking_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * ParkingSlotService Class
 * 
 * Purpose: Manages all operations related to parking slots themselves (not the bookings).
 * This includes owners creating new slots, users searching for slots, owners updating prices/capacity,
 * and checking realtime slot availability.
 */
@Service
public class ParkingSlotService {

        // Dependency Injection
        private final ParkingSlotRepository slotRepository;
        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;

        public ParkingSlotService(
                        ParkingSlotRepository slotRepository,
                        BookingRepository bookingRepository,
                        UserRepository userRepository) {
                this.slotRepository = slotRepository;
                this.bookingRepository = bookingRepository;
                this.userRepository = userRepository;
        }

        // -------------------------------
        // CREATE PARKING SLOT
        // -------------------------------
        
        /**
         * Allows an Owner to register a new parking location on the platform.
         * The slot starts in a 'PENDING' state until an Admin approves it.
         */
        @Transactional
        public ParkingSlotResponseDTO createSlot(ParkingSlotRequestDTO dto) {
                // 1. 🔐 Get logged-in user from JWT (Authentication context)
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                User owner = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // 2. 🔒 Security Check: Only a user with the 'OWNER' role can create a parking slot
                if (owner.getRole() != Role.OWNER) {
                        throw new RuntimeException("Only owners can create parking slots");
                }

                // 3. Create a blank instance of the entity class and map data from the incoming Request DTO
                ParkingSlot slot = new ParkingSlot();
                slot.setLocation(dto.getLocation());
                slot.setLatitude(dto.getLatitude());
                slot.setLongitude(dto.getLongitude());
                slot.setCarCapacity(dto.getCarCapacity());
                slot.setBikeCapacity(dto.getBikeCapacity());
                slot.setTruckCapacity(dto.getTruckCapacity());
                slot.setCarPricePerHour(dto.getCarPricePerHour());
                slot.setBikePricePerHour(dto.getBikePricePerHour());
                slot.setTruckPricePerHour(dto.getTruckPricePerHour());
                
                // Additional descriptive fields
                slot.setImageUrl(dto.getImageUrl());
                slot.setAddress(dto.getAddress());
                slot.setCity(dto.getCity());
                slot.setReviews(dto.getReviews());
                slot.setUpiId(dto.getUpiId());
                
                // Extremely important: New slots must be verified by an admin before going live to users
                slot.setStatus("PENDING"); // Explicitly set to pending
                slot.setOwner(owner); // Link the slot to this specific owner
                
                // Save to Database
                ParkingSlot saved = slotRepository.save(slot);
                
                // 4. Return the saved data back to the user as a clean Response DTO
                return new ParkingSlotResponseDTO(
                                saved.getId(),
                                saved.getLocation(),
                                saved.getLatitude(),
                                saved.getLongitude(),
                                saved.getCarCapacity(),
                                saved.getBikeCapacity(),
                                saved.getTruckCapacity(),
                                saved.getImageUrl(),
                                saved.getAddress(),
                                saved.getCity(),
                                saved.getReviews(),
                                saved.getUpiId(),
                                saved.isEnabled(),
                                saved.getCarPricePerHour(),
                                saved.getBikePricePerHour(),
                                saved.getTruckPricePerHour());
        }

        // -----------------------------------
        // GET ALL PARKING SLOTS (DTO)
        // -----------------------------------
        
        /**
         * Fetches every parking slot in the system, converting each database record into a frontend-safe DTO.
         */
        @Transactional(readOnly = true)
        public List<ParkingSlotResponseDTO> getAllSlots() {
                return slotRepository.findAll()
                                .stream()
                                .map(slot -> new ParkingSlotResponseDTO(
                                                slot.getId(),
                                                slot.getLocation(),
                                                slot.getLatitude(),
                                                slot.getLongitude(),
                                                slot.getCarCapacity(),
                                                slot.getBikeCapacity(),
                                                slot.getTruckCapacity(),
                                                slot.getImageUrl(),
                                                slot.getAddress(),
                                                slot.getCity(),
                                                slot.getReviews(),
                                                slot.getUpiId(),
                                                slot.isEnabled(),
                                                slot.getCarPricePerHour(),
                                                slot.getBikePricePerHour(),
                                                slot.getTruckPricePerHour()))
                                .toList();
        }

        /**
         * Get all parking slots natively (Returns the raw Entity object, typically used for internal server logic).
         */
        @Transactional(readOnly = true)
        public List<ParkingSlot> getAllSlotsForMap() {
                return slotRepository.findAll();
        }

        /**
         * Allows an owner to temporarily disable their parking slot (e.g., for maintenance).
         * A disabled slot will not accept new bookings but won't be deleted.
         */
        @Transactional
        @SuppressWarnings("null")
        public ParkingSlotResponseDTO setSlotEnabled(Long slotId, boolean enabled) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // 🔐 Ownership check: Prevent Owner A from disabling Owner B's slot
                if (!slot.getOwner().getEmail().equals(email)) {
                        throw new RuntimeException("You are not the owner of this parking slot");
                }

                // Update the state and save
                slot.setEnabled(enabled);
                ParkingSlot saved = slotRepository.save(slot);

                // Return updated data
                return new ParkingSlotResponseDTO(
                                saved.getId(),
                                saved.getLocation(),
                                saved.getLatitude(),
                                saved.getLongitude(),
                                saved.getCarCapacity(),
                                saved.getBikeCapacity(),
                                saved.getTruckCapacity(),
                                saved.getImageUrl(),
                                saved.getAddress(),
                                saved.getCity(),
                                saved.getReviews(),
                                saved.getUpiId(),
                                saved.isEnabled(),
                                saved.getCarPricePerHour(),
                                saved.getBikePricePerHour(),
                                saved.getTruckPricePerHour());
        }

        /**
         * Allows an owner to change the maximum number of cars/bikes/trucks that can fit in their slot.
         */
        @Transactional
        @SuppressWarnings("null")
        public ParkingSlotResponseDTO updateCapacity(
                        Long slotId,
                        int carCapacity,
                        int bikeCapacity,
                        int truckCapacity) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // Ownership check
                if (!slot.getOwner().getEmail().equals(email)) {
                        throw new RuntimeException("You are not the owner of this parking slot");
                }

                slot.setCarCapacity(carCapacity);
                slot.setBikeCapacity(bikeCapacity);
                slot.setTruckCapacity(truckCapacity);

                ParkingSlot saved = slotRepository.save(slot);

                return new ParkingSlotResponseDTO(
                                saved.getId(),
                                saved.getLocation(),
                                saved.getLatitude(),
                                saved.getLongitude(),
                                saved.getCarCapacity(),
                                saved.getBikeCapacity(),
                                saved.getTruckCapacity(),
                                saved.getImageUrl(),
                                saved.getAddress(),
                                saved.getCity(),
                                saved.getReviews(),
                                saved.getUpiId(),
                                saved.isEnabled(),
                                saved.getCarPricePerHour(),
                                saved.getBikePricePerHour(),
                                saved.getTruckPricePerHour());
        }

        // -----------------------------------
        // SLOT AVAILABILITY PREVIEW
        // -----------------------------------
        
        /**
         * Calculates REAL-TIME availability for a specific parking slot.
         * Math: Available = (Max Capacity) - (Currently Active Bookings)
         */
        @Transactional(readOnly = true)
        @SuppressWarnings("null")
        public SlotAvailabilityResponseDTO getSlotAvailability(Long slotId) {

                // Get base details (Max capacities)
                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // Count active bookings for each vehicle type individually
                Long carOccupiedObj = bookingRepository
                                .countByParkingSlot_IdAndVehicle_VehicleTypeAndActiveTrue(
                                                slotId, VehicleType.CAR);
                long carOccupied = carOccupiedObj != null ? carOccupiedObj : 0L;

                Long bikeOccupiedObj = bookingRepository
                                .countByParkingSlot_IdAndVehicle_VehicleTypeAndActiveTrue(
                                                slotId, VehicleType.BIKE);
                long bikeOccupied = bikeOccupiedObj != null ? bikeOccupiedObj : 0L;

                Long truckOccupiedObj = bookingRepository
                                .countByParkingSlot_IdAndVehicle_VehicleTypeAndActiveTrue(
                                                slotId, VehicleType.TRUCK);
                long truckOccupied = truckOccupiedObj != null ? truckOccupiedObj : 0L;

                // Return a combined DTO showing Capacity vs Occupied vs Available for all 3 types
                return new SlotAvailabilityResponseDTO(
                                slotId,

                                slot.getCarCapacity(),
                                (int) carOccupied,
                                slot.getCarCapacity() - (int) carOccupied,

                                slot.getBikeCapacity(),
                                (int) bikeOccupied,
                                slot.getBikeCapacity() - (int) bikeOccupied,

                                slot.getTruckCapacity(),
                                (int) truckOccupied,
                                slot.getTruckCapacity() - (int) truckOccupied);
        }

        /**
         * SEARCH PARKING SLOTS
         * This method filters all existing parking slots based on what the User types in the search bar.
         */
        @Transactional(readOnly = true)
        public List<ParkingSlotResponseDTO> searchParkingSlots(String location, String vehicleType) {
                // 1. Fetch ALL slots from the database
                List<ParkingSlot> allSlots = slotRepository.findAll();

                // 2. Stream through them and filter out the ones that don't match criteria
                return allSlots.stream()
                                .filter(slot -> {
                                        // A. Filter by Location Search String 
                                        // We check if the typed text is contained in the slot's Location Name, City, or Address
                                        if (location != null && !location.trim().isEmpty()) {
                                                String search = location.toLowerCase();
                                                boolean matchLoc = slot.getLocation().toLowerCase().contains(search);
                                                boolean matchCity = slot.getCity() != null
                                                                && slot.getCity().toLowerCase().contains(search);
                                                boolean matchAddress = slot.getAddress() != null
                                                                && slot.getAddress().toLowerCase().contains(search);
                                                // If it didn't match any of the geographic fields, drop it from results
                                                if (!matchLoc && !matchCity && !matchAddress) {
                                                        return false; // Skip this slot
                                                }
                                        }

                                        // B. Filter by Vehicle Type (e.g., if user searches for "BIKE" slots)
                                        // We drop the result if the slot's max capacity for that vehicle type is 0
                                        if (vehicleType != null && !vehicleType.trim().isEmpty()) {
                                                try {
                                                        VehicleType type = VehicleType
                                                                        .valueOf(vehicleType.toUpperCase());
                                                        boolean hasCapacity = switch (type) {
                                                                case CAR -> slot.getCarCapacity() > 0;
                                                                case BIKE -> slot.getBikeCapacity() > 0;
                                                                case TRUCK -> slot.getTruckCapacity() > 0;
                                                        };
                                                        if (!hasCapacity)
                                                                return false; // Skip if this location doesn't accept this vehicle
                                                } catch (IllegalArgumentException e) {
                                                        // Ignore invalid types passed from frontend
                                                }
                                        }
                                        
                                        // C. Security/Policy Filter: Only show slots that an Admin has explicitly "APPROVED"
                                        if (!"APPROVED".equals(slot.getStatus())) {
                                                return false;
                                        }

                                        // If it survived all filters, Keep this slot!
                                        return true; 
                                })
                                // 3. Convert all the surviving matching Entity slots into safe DTOs to send to the phone app/website
                                .map(slot -> new ParkingSlotResponseDTO(
                                                slot.getId(),
                                                slot.getLocation(),
                                                slot.getLatitude(),
                                                slot.getLongitude(),
                                                slot.getCarCapacity(),
                                                slot.getBikeCapacity(),
                                                slot.getTruckCapacity(),
                                                slot.getImageUrl(),
                                                slot.getAddress(),
                                                slot.getCity(),
                                                slot.getReviews(),
                                                slot.getUpiId(),
                                                slot.isEnabled(),
                                                slot.getCarPricePerHour(),
                                                slot.getBikePricePerHour(),
                                                slot.getTruckPricePerHour())) 
                                .toList();
        }

        /**
         * Finds all parking slots belonging to the currently logged-in Owner.
         */
        @Transactional(readOnly = true)
        public List<ParkingSlotResponseDTO> getSlotsByOwner() {
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                return slotRepository.findByOwner_Email(email)
                                .stream()
                                .map(slot -> new ParkingSlotResponseDTO(
                                                slot.getId(),
                                                slot.getLocation(),
                                                slot.getLatitude(),
                                                slot.getLongitude(),
                                                slot.getCarCapacity(),
                                                slot.getBikeCapacity(),
                                                slot.getTruckCapacity(),
                                                slot.getImageUrl(),
                                                slot.getAddress(),
                                                slot.getCity(),
                                                slot.getReviews(),
                                                slot.getUpiId(),
                                                slot.isEnabled(),
                                                slot.getCarPricePerHour(),
                                                slot.getBikePricePerHour(),
                                                slot.getTruckPricePerHour()))
                                .toList();
        }

        // Note: Similar to OwnerDashboardService.getSummary(). This appears to be a duplicate method.
        @Transactional(readOnly = true)
        public com.smartparking.smart_parking_backend.dto.OwnerDashboardSummary getOwnerSummary() {
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                List<ParkingSlot> slots = slotRepository.findByOwner_Email(email);
                int totalSlots = slots.size();
                int totalCar = slots.stream().mapToInt(ParkingSlot::getCarCapacity).sum();
                int totalBike = slots.stream().mapToInt(ParkingSlot::getBikeCapacity).sum();
                int totalTruck = slots.stream().mapToInt(ParkingSlot::getTruckCapacity).sum();

                Long activeBookingsObj = bookingRepository.countByParkingSlot_Owner_EmailAndActiveTrue(email);
                long activeBookings = activeBookingsObj != null ? activeBookingsObj : 0L;
                
                Double totalRevenueObj = bookingRepository.getTotalRevenueByOwnerEmail(email);
                double totalRevenue = totalRevenueObj != null ? totalRevenueObj : 0.0;

                return new com.smartparking.smart_parking_backend.dto.OwnerDashboardSummary(
                                totalSlots, totalCar, totalBike, totalTruck, activeBookings, totalRevenue);
        }

        /**
         * Allows an Owner to change how much they charge per hour for different vehicles.
         */
        @Transactional
        @SuppressWarnings("null")
        public ParkingSlotResponseDTO updatePricing(Long slotId, double carPrice, double bikePrice, double truckPrice) {
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // Security check
                if (!slot.getOwner().getEmail().equals(email)) {
                        throw new RuntimeException("You are not the owner of this parking slot");
                }

                slot.setCarPricePerHour(carPrice);
                slot.setBikePricePerHour(bikePrice);
                slot.setTruckPricePerHour(truckPrice);

                ParkingSlot saved = slotRepository.save(slot);

                return new ParkingSlotResponseDTO(
                                saved.getId(),
                                saved.getLocation(),
                                saved.getLatitude(),
                                saved.getLongitude(),
                                saved.getCarCapacity(),
                                saved.getBikeCapacity(),
                                saved.getTruckCapacity(),
                                saved.getImageUrl(),
                                saved.getAddress(),
                                saved.getCity(),
                                saved.getReviews(),
                                saved.getUpiId(),
                                saved.isEnabled(),
                                saved.getCarPricePerHour(),
                                saved.getBikePricePerHour(),
                                saved.getTruckPricePerHour());
        }

        /**
         * Allows an Owner to update the UPI ID where they want to receive their earnings for this specific slot.
         */
        @Transactional
        @SuppressWarnings("null")
        public ParkingSlotResponseDTO updateUpiId(Long slotId, String upiId) {
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // Security check
                if (!slot.getOwner().getEmail().equals(email)) {
                        throw new RuntimeException("You are not the owner of this parking slot");
                }

                slot.setUpiId(upiId);
                ParkingSlot saved = slotRepository.save(slot);

                return new ParkingSlotResponseDTO(
                                saved.getId(),
                                saved.getLocation(),
                                saved.getLatitude(),
                                saved.getLongitude(),
                                saved.getCarCapacity(),
                                saved.getBikeCapacity(),
                                saved.getTruckCapacity(),
                                saved.getImageUrl(),
                                saved.getAddress(),
                                saved.getCity(),
                                saved.getReviews(),
                                saved.getUpiId(),
                                saved.isEnabled(),
                                saved.getCarPricePerHour(),
                                saved.getBikePricePerHour(),
                                saved.getTruckPricePerHour());
        }

        // -----------------------------------
        // DELETE PARKING SLOT
        // -----------------------------------
        
        /**
         * Allows an Owner to permanently delete their parking slot entirely from the system.
         */
        @Transactional
        @SuppressWarnings("null")
        public void deleteSlot(Long slotId) {
                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                ParkingSlot slot = slotRepository.findById(Long.valueOf(slotId))
                                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

                // 🔐 Ownership check
                if (!slot.getOwner().getEmail().equals(email)) {
                        throw new RuntimeException("You are not the owner of this parking slot");
                }

                // VERY IMPORTANT: Prevent deletion if cars are currently parked there to avoid database orphans
                Long activeBookingsObj = bookingRepository.countByParkingSlot_IdAndActiveTrue(slotId);
                long activeBookings = activeBookingsObj != null ? activeBookingsObj : 0L;
                if (activeBookings > 0) {
                        throw new RuntimeException("Cannot delete slot with active bookings");
                }

                slotRepository.delete(slot);
        }

}
