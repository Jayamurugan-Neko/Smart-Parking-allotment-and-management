package com.smartparking.smart_parking_backend.exception;

import java.time.LocalDateTime;

/**
 * ErrorResponse Class
 * 
 * Purpose: This acts as a standard template for sending error messages back to the frontend.
 * Instead of sending raw database errors, we send a clean JSON object containing a 
 * human-readable message and the exact time the error happened.
 */
public class ErrorResponse {

    // The human-readable error description (e.g., "User not found")
    private String message; 
    
    // The exact moment the error occurred. Helpful for developers when checking server logs.
    private LocalDateTime timestamp; 

    /**
     * Constructor
     * Whenever a new ErrorResponse is created, it automatically records the current time.
     * @param message The specific error message to display
     */
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now(); // Automatically capture current time
    }

    // --- Getters ---
    // These are required so that Spring Boot can convert this object into JSON format automatically

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}