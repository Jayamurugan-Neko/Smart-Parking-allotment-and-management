package com.smartparking.smart_parking_backend.service;

import com.smartparking.smart_parking_backend.model.ParkingSlot;
import com.smartparking.smart_parking_backend.repository.BookingRepository;
import com.smartparking.smart_parking_backend.repository.ParkingSlotRepository;
import com.smartparking.smart_parking_backend.dto.OwnerDashboardSummary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OwnerDashboardService Class
 * 
 * Purpose: Provides data specifically for the "Owner Dashboard" view on the frontend.
 * It calculates summaries like total revenue earned by the owner, total active bookings across 
 * all their properties, and their total parking capacity.
 * 
 * Key Annotation:
 * - @Service: Tells Spring Boot this class contains business logic.
 */
@Service
public class OwnerDashboardService {

    // Dependency injection of repositories to fetch data
    private final ParkingSlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public OwnerDashboardService(
            ParkingSlotRepository slotRepository,
            BookingRepository bookingRepository
    ) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Calculates a complete summary of the Owner's business performance.
     * @return OwnerDashboardSummary DTO containing consolidated stats.
     */
    public OwnerDashboardSummary getSummary() {

        // 1. Identify which Owner is asking for the dashboard (securely from the JWT token authentication)
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Fetch all parking spots owned by this specific person
        List<ParkingSlot> slots = slotRepository.findByOwner_Email(email);

        // 3. Calculate total capacity by adding up the capacity of every single slot they own
        // Streams in Java allow us to loop through data and apply math operations very cleanly
        int totalCarCapacity = slots.stream().mapToInt(ParkingSlot::getCarCapacity).sum();
        int totalBikeCapacity = slots.stream().mapToInt(ParkingSlot::getBikeCapacity).sum();
        int totalTruckCapacity = slots.stream().mapToInt(ParkingSlot::getTruckCapacity).sum();

        // 4. Ask the DB: How many cars are CURRENTLY parked at ANY of this owner's locations?
        long activeBookings =
                bookingRepository.countByParkingSlot_Owner_EmailAndActiveTrue(email);

        // 5. Ask the DB: How much money has this owner's slots generated across all time?
        double totalRevenue =
                bookingRepository.getTotalRevenueByOwnerEmail(email);

        // 6. Package all these calculations into a single Object (DTO) to send back to the frontend screen
        return new OwnerDashboardSummary(
                slots.size(), // Total number of individual parking lots they own
                totalCarCapacity,
                totalBikeCapacity,
                totalTruckCapacity,
                activeBookings,
                totalRevenue
        );
    }
}
