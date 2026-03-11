package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.ParkingSlotResponseDTO;
import com.smartparking.smart_parking_backend.service.ParkingSlotService;
import com.smartparking.smart_parking_backend.dto.SlotAvailabilityResponseDTO;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.smartparking.smart_parking_backend.dto.ParkingSlotRequestDTO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * ParkingSlotController Class
 * 
 * Purpose: This handles all HTTP requests related to Parking Slots.
 * Whether a user is searching the map, or an owner is creating/editing their slot,
 * the requests come here first.
 */
@RestController // @RestController handles web requests and automatically converts the return value into a client-understandable format (JSON).
@RequestMapping("/api/slots") // Base path for methods in this class. Every URL will start with /api/slots.
public class ParkingSlotController {
    
    private final ParkingSlotService service;

    // Dependency Injection
    public ParkingSlotController(ParkingSlotService service) {
        this.service = service;
    }

    // ==========================================
    // SLOT CREATION & MANAGEMENT (OWNER)
    // ==========================================

    /**
     * Endpoint: POST /api/slots
     * Purpose: Allows an owner to register a new parking location.
     * 
     * @param dto The raw data from the frontend. The @RequestBody annotation converts 
     *            the incoming JSON into a Java object. The @Valid annotation ensures 
     *            the data meets our rules before proceeding.
     */
    @PostMapping
    public ParkingSlotResponseDTO createSlot(
            @Valid @RequestBody ParkingSlotRequestDTO dto) {
        return service.createSlot(dto);
    }

    /**
     * Endpoint: GET /api/slots/owner
     * Purpose: Returns only the parking slots that belong to the currently logged-in owner.
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner")
    public List<ParkingSlotResponseDTO> getOwnerSlots() {
        return service.getSlotsByOwner();
    }

    /**
     * Endpoint: GET /api/slots/owner/summary
     * Purpose: Returns dashboard summary data for the logged-in owner.
     * Note: This is similar to OwnerDashboardController and might be somewhat redundant, 
     * but provides a specific slot-focused summary.
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner/summary")
    public com.smartparking.smart_parking_backend.dto.OwnerDashboardSummary getOwnerSummary() {
        return service.getOwnerSummary();
    }

    /**
     * Endpoint: PUT /api/slots/{slotId}/price
     * Purpose: Allows the owner to update the hourly parking rates for different vehicle types.
     * @param slotId The ID in the URL indicating which slot to update.
     * @param carPrice, bikePrice, truckPrice Query parameters in the URL (e.g., ?carPrice=50&bikePrice=20)
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{slotId}/price")
    public ParkingSlotResponseDTO updatePrice(@PathVariable Long slotId,
            @RequestParam double carPrice,
            @RequestParam double bikePrice,
            @RequestParam double truckPrice) {
        return service.updatePricing(slotId, carPrice, bikePrice, truckPrice);
    }

    /**
     * Endpoint: PUT /api/slots/{slotId}/upi
     * Purpose: Allows the owner to update the UPI ID used for receiving payments for this slot.
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{slotId}/upi")
    public ParkingSlotResponseDTO updateUpiId(@PathVariable Long slotId,
            @RequestParam String upiId) {
        return service.updateUpiId(slotId, upiId);
    }

    /**
     * Endpoint: PUT /api/slots/{slotId}/capacity
     * Purpose: Allows the owner to update the maximum number of vehicles their lot can hold.
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{slotId}/capacity")
    public ParkingSlotResponseDTO updateCapacity(
            @PathVariable Long slotId,
            @RequestParam int car,
            @RequestParam int bike,
            @RequestParam int truck) {
        return service.updateCapacity(slotId, car, bike, truck);
    }

    /**
     * Endpoint: PUT /api/slots/{slotId}/enable
     * Purpose: Allows an owner to manually open their parking lot for business.
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{slotId}/enable")
    public ParkingSlotResponseDTO enableSlot(@PathVariable Long slotId) {
        return service.setSlotEnabled(slotId, true);
    }

    /**
     * Endpoint: PUT /api/slots/{slotId}/disable
     * Purpose: Allows an owner to temporarily close their parking lot (e.g., for maintenance).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{slotId}/disable")
    public ParkingSlotResponseDTO disableSlot(@PathVariable Long slotId) {
        return service.setSlotEnabled(slotId, false);
    }

    /**
     * Endpoint: DELETE /api/slots/{slotId}
     * Purpose: Deletes a parking slot entirely.
     */
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{slotId}")
    public void deleteSlot(@PathVariable Long slotId) {
        service.deleteSlot(slotId);
    }

    // ==========================================
    // PUBLIC / CUSTOMER ENDPOINTS
    // ==========================================

    /**
     * Endpoint: GET /api/slots
     * Purpose: Retrieves a list of all parking slots.
     */
    @GetMapping
    public List<ParkingSlotResponseDTO> getAllSlots() {
        return service.getAllSlots();
    }

    /**
     * Endpoint: GET /api/slots/map
     * Purpose: Specifically retrieves data tailored for displaying parking slots on the frontend Map.
     * Note: This manually maps the Entity data into a fresh DTO instance.
     */
    @GetMapping("/map")
    public List<ParkingSlotResponseDTO> getSlotsForMap() {
        return service.getAllSlotsForMap()
                .stream()
                .map(slot -> new ParkingSlotResponseDTO(
                        slot.getId(),
                        slot.getLocation(),
                        slot.getLatitude(),
                        slot.getLongitude(),
                        slot.getCarCapacity(),
                        slot.getBikeCapacity(),
                        slot.getTruckCapacity(),
                        slot.getImageUrl(),
                        slot.getAddress(),
                        slot.getCity(),
                        slot.getReviews(),
                        slot.getUpiId(), 
                        slot.isEnabled(), 
                        slot.getCarPricePerHour(),
                        slot.getBikePricePerHour(),
                        slot.getTruckPricePerHour()))
                .toList();
    }

    /**
     * Endpoint: GET /api/slots/{slotId}/availability
     * Purpose: Checks if a specific parking slot has any empty spaces right now.
     */
    @GetMapping("/{slotId}/availability")
    public SlotAvailabilityResponseDTO getAvailability(@PathVariable Long slotId) {
        return service.getSlotAvailability(slotId);
    }
}
