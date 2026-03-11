package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.VehicleRequestDTO;
import com.smartparking.smart_parking_backend.dto.VehicleResponseDTO;
import com.smartparking.smart_parking_backend.model.Vehicle;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.repository.UserRepository;
import com.smartparking.smart_parking_backend.repository.VehicleRepository;
import com.smartparking.smart_parking_backend.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.util.List;

/**
 * VehicleController Class
 * 
 * Purpose: Manages the vehicles associated with a user's account.
 * Allows users to add multiple cars or bikes to their profile so they can easily 
 * select one when making a parking booking.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    // Dependency Injection
    public VehicleController(VehicleRepository vehicleRepository, UserRepository userRepository) {
            this.vehicleRepository = vehicleRepository;
            this.userRepository = userRepository;
    }

    /**
     * Endpoint: GET /api/vehicles/me
     * Purpose: Returns a list of all vehicles owned by the currently logged-in user.
     */
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getMyVehicles() {
            // Find who is requesting this (via their JWT secure token)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Query the database for ALL vehicles linked to this specific user ID
            return vehicleRepository.findByUser(user)
                            .stream()
                            .map(vehicle -> new VehicleResponseDTO(
                                            vehicle.getId(),
                                            vehicle.getVehicleNumber(),
                                            vehicle.getVehicleModel(),
                                            vehicle.getVehicleType(),
                                            vehicle.getOwnerName()))
                            .toList();
    }

    /**
     * Endpoint: POST /api/vehicles
     * Purpose: Adds a new vehicle to the currently logged-in user's profile.
     * 
     * @param dto JSON containing vehicle properties (like "vehicleNumber" and "vehicleType").
     */
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @PostMapping
    @Transactional
    public VehicleResponseDTO createVehicle(@Valid @RequestBody VehicleRequestDTO dto) {
            
            // 1. Identify the user making the request
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 2. Build a new Vehicle Entity object based on the incoming JSON data
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleNumber(dto.getVehicleNumber());
            vehicle.setVehicleModel(dto.getVehicleModel() != null ? dto.getVehicleModel() : "");
            vehicle.setVehicleType(dto.getVehicleType());
            
            // If the user didn't specify an owner name for the car, just use their account name
            vehicle.setOwnerName(dto.getOwnerName() != null ? dto.getOwnerName() : user.getName());
            
            // 3. SECONDS MOST IMPORTANT STEP - link the vehicle to the user!
            vehicle.setUser(user);

            // Save to DB
            Vehicle savedVehicle = vehicleRepository.save(vehicle);

            return new VehicleResponseDTO(
                            savedVehicle.getId(),
                            savedVehicle.getVehicleNumber(),
                            savedVehicle.getVehicleModel(),
                            savedVehicle.getVehicleType(),
                            savedVehicle.getOwnerName());
    }

    /**
     * Endpoint: DELETE /api/vehicles/{id}
     * Purpose: Deletes a specific vehicle from the user's profile.
     */
    @PreAuthorize("hasAnyRole('USER', 'OWNER')") // Ensures only logged-in users can access this
    @DeleteMapping("/{id}")
    @Transactional
    public void deleteVehicle(@PathVariable Long id) {
            
            // 1. Get the current logged-in user's email from the security context
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 2. Find the vehicle the user is trying to delete based on the URL ID
            Vehicle vehicle = vehicleRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

            // 3. SECURITY CHECK: Ensure the user TRYING to delete the vehicle is actually the OWNER.
            // We don't want User A deleting User B's vehicle by guessing the URL ID!
            if (!vehicle.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("You are not authorized to delete this vehicle");
            }

            // 4. Finally, it's safe to remove it from the database
            vehicleRepository.delete(vehicle);
    }
}
