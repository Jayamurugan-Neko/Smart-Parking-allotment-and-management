package com.smartparking.smart_parking_backend.service;

import com.smartparking.smart_parking_backend.dto.AdminStatsDTO;
import com.smartparking.smart_parking_backend.dto.OwnerPayoutDTO;
import com.smartparking.smart_parking_backend.model.ParkingSlot;
import com.smartparking.smart_parking_backend.model.Payout;
import com.smartparking.smart_parking_backend.model.Role;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminService Class
 * 
 * Purpose: Contains the core business logic strictly meant for Administrators.
 * Handles tasks like fetching global system statistics, managing user payouts, 
 * verifying new parking slots, and user administration.
 * 
 * Key Annotation:
 * - @Service: Tells Spring Boot this class contains business logic, allowing it to be injected wherever needed.
 */
@Service
public class AdminService {

    // Repositories injected to interact with the database
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PayoutRepository payoutRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    // Constructor Dependency Injection - Spring automatically provides these repositories when creating the AdminService
    public AdminService(BookingRepository bookingRepository, UserRepository userRepository,
            PayoutRepository payoutRepository, ParkingSlotRepository parkingSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.payoutRepository = payoutRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }

    /**
     * Calculates combined statistics for the entire platform to display on the Admin Dashboard.
     * @return AdminStatsDTO containing counts of users, owners, bookings, revenue, and pending slots.
     */
    public AdminStatsDTO getGlobalStats() {
        long totalUsers = userRepository.count();
        // Filters the entire user list to count only those with the OWNER role
        long totalOwners = userRepository.findAll().stream().filter(u -> Role.OWNER.equals(u.getRole())).count();
        long totalBookings = bookingRepository.count();
        // Safely gets total revenue, defaulting to 0.0 if there's no revenue yet
        double totalRevenue = bookingRepository.getTotalRevenue() != null ? bookingRepository.getTotalRevenue() : 0.0;
        // Counts how many parking slots are waiting for admin approval
        long pendingSlots = parkingSlotRepository.findAll().stream().filter(s -> "PENDING".equals(s.getStatus())).count();

        return new AdminStatsDTO(totalUsers, totalOwners, totalBookings, totalRevenue, pendingSlots);
    }

    /**
     * Calculates how much money each Owner has earned vs. how much they have actually been paid out by the platform.
     * @return A list of OwnerPayoutDTOs used to display a payout table to the admin.
     */
    public List<OwnerPayoutDTO> getOwnersPayoutStatus() {
        // Fetch all users who are registered as Owners
        List<User> owners = userRepository.findAll().stream()
                .filter(u -> Role.OWNER.equals(u.getRole()))
                .toList();

        List<OwnerPayoutDTO> stats = new ArrayList<>();

        // Loop through each owner to calculate their specific financials
        for (User owner : owners) {
            Double totalEarned = bookingRepository.getTotalConfirmedRevenueByOwnerId(owner.getId());
            Double totalPaid = payoutRepository.getTotalPaidToOwner(owner.getId());

            // Handle cases where they haven't earned or been paid anything yet
            if (totalEarned == null) totalEarned = 0.0;
            if (totalPaid == null) totalPaid = 0.0;

            String upiId = "N/A";

            // 1. Logic to find the owner's UPI ID: Try to find UPI from any of the owner's registered slots first
            if (owner.getParkingSlots() != null) {
                for (ParkingSlot slot : owner.getParkingSlots()) {
                    if (slot.getUpiId() != null && !slot.getUpiId().isEmpty()) {
                        upiId = slot.getUpiId();
                        break; 
                    }
                }
            }

            // 2. Logic to find the owner's UPI ID: Fallback to the Owner's Profile UPI if not found attached to a slot
            if ("N/A".equals(upiId) && owner.getUpiId() != null && !owner.getUpiId().isEmpty()) {
                upiId = owner.getUpiId();
            }

            // Pack the calculated data into a DTO (Data Transfer Object) to send to the frontend
            stats.add(new OwnerPayoutDTO(
                    owner.getId(),
                    owner.getName(),
                    owner.getEmail(),
                    upiId,
                    totalEarned,
                    totalPaid));
        }
        return stats;
    }

    /**
     * Records a manual payout made offline by the Admin to the Owner.
     */
    public void processPayout(Long ownerId, double amount) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));

        // Creates a new payout record and saves it to the database
        Payout payout = new Payout(owner, amount, "PAID");
        payoutRepository.save(payout);
    }

    /**
     * Retrieves all parking slots that need manual verification by the admin.
     * @Transactional(readOnly = true) speeds up reading data by telling Hibernate it doesn't need to track changes.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ParkingSlot> getPendingSlots() {
        return parkingSlotRepository.findAll().stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .toList();
    }

    /**
     * Approves or Rejects a parking slot, optionally leaving a comment explaining the decision.
     */
    public void verifySlot(Long slotId, String status, String comments) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        slot.setStatus(status);
        if (comments != null && !comments.isEmpty()) {
            slot.setAdminComments(comments);
        }
        parkingSlotRepository.save(slot);
    }

    /**
     * Helper method to fetch the system administrator's profile.
     * Assumes "admin@smartparking.com" is the hardcoded master admin account.
     */
    public User getAdminProfile() {
        return userRepository.findByEmail("admin@smartparking.com")
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    /**
     * Updates the master UPI ID for the system where platform payments will be routed.
     */
    public void updateAdminUpi(String upiId) {
        User admin = getAdminProfile();
        admin.setUpiId(upiId);
        userRepository.save(admin);
    }

    /**
     * Retrieves a list of all users registered in the system (Users, Owners, Admins).
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user by their ID.
     */
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }
}
