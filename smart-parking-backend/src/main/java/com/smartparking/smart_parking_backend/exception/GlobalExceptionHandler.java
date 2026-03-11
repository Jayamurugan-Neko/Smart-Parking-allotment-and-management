package com.smartparking.smart_parking_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler Class
 * 
 * Purpose: Acts like a global "safety net" or "security guard" for the entire application.
 * If any Controller throws an error, this class catches it before it reaches the user,
 * formats it cleanly, and returns the appropriate HTTP status code (e.g., 404, 400, 500).
 * 
 * Key Annotation:
 * - @RestControllerAdvice: Tells Spring Boot to listen for exceptions across ALL controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches ResourceNotFoundException.
     * This happens when we try to look up something in the DB (like a User or Slot) that doesn't exist.
     * 
     * @return HTTP 404 (NOT_FOUND) with the error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class) 
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Catches SlotAlreadyBookedException.
     * This happens if two users try to book the same slot at the exact same millisecond,
     * or if a user tries to book a slot that is already full.
     * 
     * @return HTTP 400 (BAD_REQUEST) with the error message.
     */
    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<String> handleSlotBooked(SlotAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Catches ALL other generic exceptions that we haven't specifically planned for 
     * (e.g., NullPointerExceptions, database connection failures).
     * This prevents the server from crashing or exposing sensitive stack traces to the user.
     * 
     * @return HTTP 500 (INTERNAL_SERVER_ERROR) with a generic safe message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception ex) {
        // Log the actual error to the server console for developers to fix
        ex.printStackTrace();
        
        // Return a generic, safe message to the frontend user
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }
}