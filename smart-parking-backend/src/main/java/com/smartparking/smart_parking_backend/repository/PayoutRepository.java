package com.smartparking.smart_parking_backend.repository;

import com.smartparking.smart_parking_backend.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PayoutRepository Interface
 * 
 * Purpose: Connects the Payout model to the database.
 * Allows the application to save new payouts and retrieve payout history for owners.
 */
@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    
    // Retrieves a list of all payouts transferred to a specific Owner User ID
    List<Payout> findByOwnerId(Long ownerId);

    /**
     * Custom Query to calculate the total sum of all money ever successfully paid to a specific owner.
     * Uses JPQL to sum the 'amount' field where the status is precisely 'PAID'.
     */
    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.owner.id = :ownerId AND p.status = 'PAID'")
    Double getTotalPaidToOwner(Long ownerId);
}
