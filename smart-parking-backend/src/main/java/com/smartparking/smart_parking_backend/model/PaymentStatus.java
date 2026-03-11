package com.smartparking.smart_parking_backend.model;

/**
 * PaymentStatus Enum
 * 
 * Purpose: Defines the restricted set of possible statuses a booking's payment can have.
 * Using an Enum ensures that the code can only set the payment status to one of these three exact values, preventing typos like "pendng" or "complet".
 */
public enum PaymentStatus {
    PENDING,   // The user has initiated the booking but hasn't paid yet.
    COMPLETED, // The user has successfully paid for the booking.
    FAILED     // The payment attempt failed.
}
