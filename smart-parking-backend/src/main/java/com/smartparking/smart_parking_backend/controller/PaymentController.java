package com.smartparking.smart_parking_backend.controller;

import com.smartparking.smart_parking_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PaymentController Class
 * 
 * Purpose: This acts as the bridge between your frontend (React) and the Razorpay payment gateway.
 * It handles the two crucial steps of online payments:
 * 1. Generating a secure "Order ID" from Razorpay before the payment happens.
 * 2. Cryptographically verifying the payment was actually successful after the user pays.
 */
@RestController
@RequestMapping("/api/payment")
// @CrossOrigin(origins = "*") // Removed to use global CORS with credentials
public class PaymentController {

    private final PaymentService paymentService;

    // Dependency Injection
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint: POST /api/payment/create-order
     * Purpose: STEP 1 - Initialize the payment.
     * The frontend asks the backend to create an official "Order" with Razorpay for a specific booking.
     * 
     * @param payload JSON containing the 'bookingId' the user wants to pay for.
     * @return A JSON response direct from Razorpay containing the new 'order_id' and the amount in paise.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Long> payload) {
        try {
            Long bookingId = payload.get("bookingId");
            
            // The service layer contacts Razorpay's servers over the internet
            String orderResponse = paymentService.createOrder(bookingId);
            
            // Send the new Order ID back to the frontend so it can open the checkout popup
            return ResponseEntity.ok(orderResponse);
            
        } catch (Exception e) {
            // If Razorpay is down, or the booking is invalid, return a 500 error safely
            return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
        }
    }

    /**
     * Endpoint: POST /api/payment/verify-payment
     * Purpose: STEP 2 - Confirm the payment is real.
     * After the user pays on the frontend, Razorpay gives the frontend a signature. 
     * The frontend sends that signature here so the backend can verify it wasn't forged.
     * 
     * @param payload JSON containing the order ID, payment ID, and the cryptographic signature.
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String orderId = payload.get("razorpay_order_id");
            String paymentId = payload.get("razorpay_payment_id");
            String signature = payload.get("razorpay_signature");

            // The service layer uses our secret key to check if the signature is genuine
            String result = paymentService.verifyPayment(orderId, paymentId, signature);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // If the signature is fake or tampered with, this catches the error and rejects the payment
            return ResponseEntity.status(400).body("Verification failed: " + e.getMessage());
        }
    }
}
