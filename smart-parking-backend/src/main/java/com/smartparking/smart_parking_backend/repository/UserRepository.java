package com.smartparking.smart_parking_backend.repository;

import com.smartparking.smart_parking_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * UserRepository Interface
 * 
 * Purpose: Connects the User model to the database.
 * Handles tasks like finding a user during login or checking if an email is already registered.
 * 
 * Note: By extending JpaRepository, Spring automatically figures out how to talk to the database.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Finds a user by their email address. Used constantly for Login authentication.
    // Returns Optional<> because a user with that exact email might not exist.
    Optional<User> findByEmail(String email);

    // Checks the database to see if a specific email is already taken.
    // Used during the User Registration process to prevent duplicate accounts.
    boolean existsByEmail(String email);
}
