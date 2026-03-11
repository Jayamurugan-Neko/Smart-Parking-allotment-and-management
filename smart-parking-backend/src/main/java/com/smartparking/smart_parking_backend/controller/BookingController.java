package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.dto.BookingRequestDTO;
import com.smartparking.smart_parking_backend.dto.BookingResponseDTO;
import com.smartparking.smart_parking_backend.service.BookingService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * BookingController Class
 * 
 * Purpose: This handles all incoming HTTP requests related to parking reservations.
 * If a customer wants to book a slot, or an owner wants to see their bookings, the frontend calls these URLs.
 * 
 * Note: By default, Spring RestControllers process request bodies and format return values as JSON.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService service;

    // Dependency Injection: Spring provides the BookingService automatically when this controller is created.
    public BookingController(BookingService service) {
        this.service = service;
    }

    /**
     * Endpoint: POST /api/bookings
     * Purpose: Creates a brand new parking reservation.
     * 
     * @param dto The data sent from the frontend (slot ID, vehicle info, start/end time).
     *            The `@Valid` annotation tells Spring to check the DTO's rules (e.g., "startTime cannot be null") 
     *            BEFORE running the method. If validation fails, it instantly returns a 400 Bad Request error.
     * @return A BookingResponseDTO containing the confirmed booking details and calculated final price.
     */
    @PostMapping
    public BookingResponseDTO bookSlot(
            @Valid @RequestBody BookingRequestDTO dto) {
        
        // Pass the validated data down to the Service layer where the actual database checks 
        // (like "Is this slot already full?") happen.
        return service.bookSlot(dto);
    }

    /**
     * Endpoint: GET /api/bookings/me
     * Purpose: Allows a Customer to view all of their past and present bookings.
     * Note: We don't pass a User ID in the URL. The service extracts the User ID securely 
     * from the JWT token in the HTTP Header to prevent users from viewing others' bookings.
     */
    @GetMapping("/me")
    public List<BookingResponseDTO> getMyBookings() {
        return service.getMyBookings();
    }

    /**
     * Endpoint: GET /api/bookings/owner
     * Purpose: Allows a Parking Lot Owner to view all bookings made at THEIR slots by customers.
     */
    @GetMapping("/owner")
    public List<BookingResponseDTO> getOwnerBookings() {
        return service.getOwnerBookings();
    }

    /**
     * Endpoint: POST /api/bookings/{bookingId}/end
     * Purpose: Allows an owner (or admin) to mark a currently active booking as "COMPLETED" 
     * when the vehicle exits the parking lot.
     * 
     * @param bookingId Extracted from the URL (e.g., /api/bookings/5/end means bookingId = 5).
     */
    @PostMapping("/{bookingId}/end")
    public BookingResponseDTO endBooking(@PathVariable Long bookingId) {
        return service.endBooking(bookingId);
    }

    /**
     * Endpoint: POST /api/bookings/{bookingId}/pay
     * Purpose: Marks a booking's physical payment status as paid.
     * Note: This seems to handle offline or manual payments, as Razorpay integration relies on `PaymentController`.
     * 
     * @param bookingId The ID of the booking to pay for.
     * @param paymentRef An optional reference string (like a cash receipt number or UPI transaction ID). 
     *                   `required = false` means the API won't crash if the frontend doesn't send a body.
     */
    @PostMapping("/{bookingId}/pay")
    public BookingResponseDTO payBooking(@PathVariable Long bookingId,
            @RequestBody(required = false) String paymentRef) {
        return service.processPayment(bookingId, paymentRef);
    }
}
