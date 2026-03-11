package com.smartparking.smart_parking_backend.model;

/**
 * Role Enum
 * 
 * Purpose: Defines the three types of users in the system.
 * Used for authorization to ensure users can only access features meant for their role.
 */
public enum Role {
    USER,  // A standard user who searches for and books parking slots.
    OWNER, // A property owner who lists their parking slots for rent.
    ADMIN  // An administrator who manages users, approves parking slots, and views system stats.
}
