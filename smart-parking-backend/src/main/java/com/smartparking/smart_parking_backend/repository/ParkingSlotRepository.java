package com.smartparking.smart_parking_backend.repository;

import com.smartparking.smart_parking_backend.model.ParkingSlot;
import com.smartparking.smart_parking_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

/**
 * ParkingSlotRepository Interface
 * 
 * Purpose: This interface handles all database operations (Data Access Layer) for 'ParkingSlot' entities. 
 * We write ZERO boilerplate SQL; Spring generates a proxy implementation of this interface at runtime.
 * 
 * Key Annotations:
 * - @Repository: Tells Spring Boot that this is a database service and registers it in the Spring Application Context.
 */
@Repository 
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    
    /**
     * Custom method to fetch a slot and lock it to prevent double-booking.
     * 
     * @Lock(LockModeType.PESSIMISTIC_WRITE): Database-level Exclusive lock. 
     * It tells the DB: "If I select this row, lock it right now. Do not let anyone else read or write to it until my transaction finishes."
     * 
     * @Query: Uses JPQL (Java Persistence Query Language) to find a specific slot by its ID.
     * @Param("id"): Links the method's Java parameter 'id' to the ':id' variable inside the SQL query, preventing SQL injection hacks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.id = :id")
    Optional<ParkingSlot> findSlotForUpdate(@Param("id") Long id);

    // Counts the total number of parking slots registered by a specific owner's email
    long countByOwner_Email(String email);

    // Returns a list of all parking slots belonging to an owner's email
    List<ParkingSlot> findByOwner_Email(String email);

    // Returns a list of all parking slots belonging to a specific Owner User object
    List<ParkingSlot> findByOwner(User owner);
}