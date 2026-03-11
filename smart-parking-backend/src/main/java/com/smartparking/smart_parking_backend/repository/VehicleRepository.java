package com.smartparking.smart_parking_backend.repository;

import com.smartparking.smart_parking_backend.model.Vehicle;
import com.smartparking.smart_parking_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VehicleRepository Interface
 * 
 * Purpose: Connects the Vehicle model to the database.
 * Used for fetching, adding, or deleting user vehicles.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    // Finds and returns a list of all vehicles that belong to a specific User.
    // Automatically generates the SQL query to match the user_id column in the vehicles table.
    List<Vehicle> findByUser(User user);
}