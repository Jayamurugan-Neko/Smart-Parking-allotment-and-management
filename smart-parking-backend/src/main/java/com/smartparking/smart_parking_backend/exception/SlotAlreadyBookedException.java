package com.smartparking.smart_parking_backend.exception;

/**
 * Custom Exception: SlotAlreadyBookedException
 * 
 * Purpose: Thrown specifically during the booking process if a user tries to book a parking slot
 * that has zero remaining capacity for their vehicle type.
 * 
 * It helps differentiate between a generic "Bad Request" and a specific "Parking Full" scenario.
 */
public class SlotAlreadyBookedException extends RuntimeException {
    
    /**
     * Constructor
     * @param message Detail about the error (e.g., "Car capacity reached for this slot")
     */
    public SlotAlreadyBookedException(String message) {
        super(message);
    }
}