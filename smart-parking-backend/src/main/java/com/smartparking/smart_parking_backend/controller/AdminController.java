package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.AdminStatsDTO;
import com.smartparking.smart_parking_backend.dto.OwnerPayoutDTO;
import com.smartparking.smart_parking_backend.model.ParkingSlot;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AdminController Class
 * 
 * Purpose: This is the "Front Desk" for all Administrator operations.
 * When the frontend (React/Mobile) needs admin data (like total stats, user lists, 
 * or approving parking slots), it sends HTTP requests here.
 * 
 * Key Annotations:
 * - @RestController: Tells Spring Boot this class handles web requests and automatically converts the returned data into JSON format for the frontend.
 * - @RequestMapping("/api/admin"): Sets the base URL for every endpoint in this file. Thus, every URL here starts with "http://localhost:8080/api/admin".
 * 
 * Security Note: 
 * Because of our SecurityConfig, ONLY users with the "ADMIN" role can successfully access these endpoints.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // The Controller doesn't do the heavy lifting itself. It passes the work to the Service layer.
    private final AdminService adminService;

    // Dependency Injection
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint: GET /api/admin/stats
     * Purpose: Returns aggregate statistics (total users, slots, revenue) for the admin dashboard.
     * @return A ResponseEntity containing the AdminStatsDTO in JSON format.
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        // ResponseEntity.ok() wraps the data in a standard HTTP 200 (Success) response
        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    /**
     * Endpoint: GET /api/admin/payouts
     * Purpose: Fetches a list of all parking lot owners and how much money they are owed.
     */
    @GetMapping("/payouts")
    public ResponseEntity<List<OwnerPayoutDTO>> getPayouts() {
        return ResponseEntity.ok(adminService.getOwnersPayoutStatus());
    }

    /**
     * Endpoint: POST /api/admin/payout
     * Purpose: Records that the admin has paid an owner their earnings.
     * @param payload A JSON body containing "ownerId" and "amount". @RequestBody automatically maps the JSON into a Java Map.
     */
    @PostMapping("/payout")
    public ResponseEntity<?> createPayout(@RequestBody Map<String, Object> payload) {
        // Extract data from the incoming JSON payload
        Long ownerId = Long.valueOf(payload.get("ownerId").toString());
        double amount = Double.parseDouble(payload.get("amount").toString());

        // Tell the service layer to process the payment logic in the database
        adminService.processPayout(ownerId, amount);
        
        // Return a simple success message
        return ResponseEntity.ok("Payout processed successfully");
    }

    // ==========================================
    // SLOT VERIFICATION MANAGEMENT
    // ==========================================

    /**
     * Endpoint: GET /api/admin/slots/pending
     * Purpose: Allows the admin to see all new parking slots that owners have submitted for approval.
     */
    @GetMapping("/slots/pending")
    public ResponseEntity<List<ParkingSlot>> getPendingSlots() {
        return ResponseEntity.ok(adminService.getPendingSlots());
    }

    /**
     * Endpoint: PUT /api/admin/slots/{id}/verify
     * Purpose: Allows the admin to Approve or Reject a specific parking slot.
     * @param id The ID of the slot in the URL (e.g., /api/admin/slots/5/verify). Captured by @PathVariable.
     * @param payload JSON containing the new "status" and any "comments".
     */
    @PutMapping("/slots/{id}/verify")
    public ResponseEntity<?> verifySlot(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status"); // Expected to be "APPROVED" or "REJECTED"
        String comments = payload.get("comments"); // E.g., "Photo is blurry, please re-upload."

        adminService.verifySlot(id, status, comments);
        return ResponseEntity.ok("Slot verification updated");
    }

    // ==========================================
    // ADMIN PROFILE MANAGEMENT
    // ==========================================

    /**
     * Endpoint: GET /api/admin/profile
     * Purpose: Gets the currently logged-in admin's own profile data (used for settings pages).
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        User admin = adminService.getAdminProfile();
        
        // Return a customized JSON object with just name, email, and upiId (omitting password/etc)
        return ResponseEntity.ok(Map.of(
                "name", admin.getName(),
                "email", admin.getEmail(),
                "upiId", admin.getUpiId() != null ? admin.getUpiId() : ""));
    }

    /**
     * Endpoint: PUT /api/admin/profile
     * Purpose: Allows the admin to update their own UPI ID.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String upiId = payload.get("upiId");
        adminService.updateAdminUpi(upiId);
        return ResponseEntity.ok("Admin profile updated");
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    /**
     * Endpoint: GET /api/admin/users
     * Purpose: Returns a list of EVERY user in the database (Customers, Owners, and Admins).
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Endpoint: DELETE /api/admin/users/{id}
     * Purpose: Permanently deletes a user account from the system.
     * @param id The ID of the user to delete, taken from the URL.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
