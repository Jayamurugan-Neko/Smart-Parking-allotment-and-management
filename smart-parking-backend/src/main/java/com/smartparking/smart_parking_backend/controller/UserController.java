package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.LoginRequestDTO;
import com.smartparking.smart_parking_backend.dto.LoginResponseDTO;
import com.smartparking.smart_parking_backend.dto.UserProfileUpdateDTO;
import com.smartparking.smart_parking_backend.dto.UserProfileResponseDTO;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.repository.UserRepository;
import com.smartparking.smart_parking_backend.repository.ParkingSlotRepository;
import com.smartparking.smart_parking_backend.security.JwtService;
import com.smartparking.smart_parking_backend.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * UserController Class
 * 
 * Purpose: Manages all user-related actions: signing up, logging in, 
 * viewing your own profile, and updating your profile details.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // These repositories and services perform database queries and logic
    private final UserRepository userRepository;
    private final ParkingSlotRepository slotRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired // Automatically injects the JwtService bean
    private JwtService jwtService;

    // Dependency Injection constructor
    public UserController(UserRepository userRepository, ParkingSlotRepository slotRepository, BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Endpoint: POST /api/users
     * Purpose: Simple user creation (usually used for testing or internal admin tools).
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    /**
     * Endpoint: POST /api/users/signup
     * Purpose: Allows new customers or owners to create an account.
     * Note: This is an unauthenticated PUBLIC endpoint (anyone can call it).
     */
    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        // ALWAYS encrypt the password before saving to the database!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    /**
     * Endpoint: POST /api/users/login
     * Purpose: Verifies user credentials and generates a secure JWT token.
     * 
     * @param dto Contains the email and plain-text password typed into the login form.
     * @return LoginResponseDTO, which contains the JWT token the frontend needs to save.
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto)
    {
        // 1. Check if a user with this email exists
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Compare the typed password (plain text) to the massive hash saved in the database
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials"); // Stop here if passwords don't match
        }

        // 3. Generate the "VIP wristband" token for this session
        String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole().name()
        );

        // 4. Check if they own any parking slots (useful for deciding which dashboard to show them)
        boolean hasSlots = slotRepository.countByOwner_Email(user.getEmail()) > 0;

        // 5. Send everything back to the frontend
        return new LoginResponseDTO(
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            hasSlots,
            user.getRole() != null ? user.getRole().name() : null
        );
    }

    /**
     * Endpoint: GET /api/users/me
     * Purpose: Retrieves the full profile details of the CURRENTLY logged-in user.
     * Security: Requires a valid JWT token. The server asks the token "Who are you?"
     *           rather than asking the frontend for a User ID.
     */
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @GetMapping("/me")
    @Transactional(readOnly = true) // Tells the database we are ONLY reading, which makes it slightly faster
    public UserProfileResponseDTO getMyProfile() {
        // Find out who is making this request using the SecurityContext (populated by our JwtFilter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Fetch their latest info from the DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Package the data nicely into a DTO (omitting sensitive info like passwords)
        return new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getCity(),
                user.getState(),
                user.getZipCode(),
                user.getCountry(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }

    /**
     * Endpoint: PUT /api/users/me
     * Purpose: Updates the profile details (like phone number or address) of the currently logged-in user.
     */
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @PutMapping("/me")
    @Transactional // Ensures the entire database update happens completely or not at all
    public UserProfileResponseDTO updateMyProfile(@RequestBody UserProfileUpdateDTO dto) {
        
        // Securely identify the user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Partially update fields ONLY if the frontend actually sent them in the JSON body
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getState() != null) {
            user.setState(dto.getState());
        }
        if (dto.getZipCode() != null) {
            user.setZipCode(dto.getZipCode());
        }
        if (dto.getCountry() != null) {
            user.setCountry(dto.getCountry());
        }

        // Save the updated user object back to the database
        User savedUser = userRepository.save(user);

        // Return the fresh data so the frontend can update its UI
        return new UserProfileResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getAddress(),
                savedUser.getCity(),
                savedUser.getState(),
                savedUser.getZipCode(),
                savedUser.getCountry(),
                savedUser.getRole() != null ? savedUser.getRole().name() : null
        );
    }
}
