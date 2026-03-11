package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.OwnerDashboardSummary;
import com.smartparking.smart_parking_backend.service.OwnerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OwnerDashboardController Class
 * 
 * Purpose: This controller handles requests specifically for the Parking Lot Owner's dashboard screen.
 * It provides a single endpoint that gathers all necessary stats (total slots, active bookings, revenue)
 * and returns them in one convenient package.
 */
@RestController // Automatically converts responses to JSON
@RequestMapping("/owner/dashboard") // Base URL for endpoints in this class
public class OwnerDashboardController {

    private final OwnerDashboardService service;

    // Dependency Injection
    public OwnerDashboardController(OwnerDashboardService service) {
        this.service = service;
    }

    /**
     * Endpoint: GET /owner/dashboard/summary
     * Purpose: Retrieves a summary of the logged-in owner's parking business.
     * 
     * Key Annotation:
     * - @PreAuthorize("hasRole('OWNER')"): This is a powerful Spring Security feature. 
     *   Even if a user is logged in (has a valid token), if their role inside the token 
     *   is NOT "OWNER" (e.g., they are just a "CUSTOMER"), Spring will automatically 
     *   block the request and return a 403 Forbidden error BEFORE this method even runs.
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/summary")
    public OwnerDashboardSummary getSummary() {
        return service.getSummary();
    }
}
