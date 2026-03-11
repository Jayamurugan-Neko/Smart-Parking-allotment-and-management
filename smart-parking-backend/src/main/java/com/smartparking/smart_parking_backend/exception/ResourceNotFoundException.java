package com.smartparking.smart_parking_backend.exception;

/**
 * Custom Exception: ResourceNotFoundException
 * 
 * Purpose: We throw this specific error whenever we try to fetch something from the database
 * but it doesn't exist. For example: Searching for User ID 999 when only 10 users exist.
 * 
 * By creating our own exception extending RuntimeException, we can catch it cleanly 
 * in the GlobalExceptionHandler and send a generic 404 response to the frontend.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructor
     * @param message The specific detail of what wasn't found (e.g., "User with email X not found")
     */
    public ResourceNotFoundException(String message) {
        super(message); // Passes the message up to the parent RuntimeException class
    }
}
