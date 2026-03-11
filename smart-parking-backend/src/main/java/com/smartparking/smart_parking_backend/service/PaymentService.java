package com.smartparking.smart_parking_backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smartparking.smart_parking_backend.exception.ResourceNotFoundException;
import com.smartparking.smart_parking_backend.model.Booking;
import com.smartparking.smart_parking_backend.model.PaymentStatus;
import com.smartparking.smart_parking_backend.repository.BookingRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat; // Java 17+

/**
 * PaymentService Class
 * 
 * Purpose: Handles integration with the Razorpay payment gateway.
 * It creates unique payment orders for specific bookings and strictly verifies 
 * that the money was successfully received before marking a booking as PAID.
 */
@Service
public class PaymentService {

    // These values are securely loaded from the application.properties file 
    // They act as the username/password to talk to the Razorpay servers
    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final BookingRepository bookingRepository;

    public PaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Step 1 of Payment: Create an "Order" on Razorpay's systems.
     * This tells Razorpay "Expect exactly X amount of money for Booking Y".
     * 
     * @param bookingId The ID of the database booking being paid for
     * @return A JSON string containing Razorpay's unique Order ID.
     */
    @Transactional
    public String createOrder(Long bookingId) {
        try {
            // Find the exact booking the user is trying to pay for
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            // Initialize the tool used to talk to Razorpay
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // Build the payment request details
            JSONObject orderRequest = new JSONObject();
            
            // Razorpay strictly requires amounts to be in "paise" (the smallest unit of currency).
            // Example: ₹10 must be sent as 1000 paise.
            int amountInPaise = (int) (booking.getTotalPrice() * 100);
            
            // Razorpay will reject any transaction under 100 paise (₹1).
            // This safety check prevents crash errors on very short parking durations.
            if (amountInPaise < 100) {
                amountInPaise = 100; // Enforce minimum ₹1
            }
            
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + bookingId); // Our internal tracking ID

            // Send the request over the internet to Razorpay to officially create the order
            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");

            // Save Razorpay's newly generated Order ID into our database so we can verify it later
            booking.setRazorpayOrderId(orderId);
            bookingRepository.save(booking);

            return order.toString(); // Returns full JSON order object back to the frontend

        } catch (RazorpayException e) {
            // If Razorpay's servers are down or keys are strictly invalid, it throws this exception
            throw new RuntimeException("Razorpay error: " + e.getMessage());
        }
    }

    /**
     * Step 2 of Payment: Verify the transaction.
     * After the user pays on the frontend, Razorpay sends back a cryptographic signature.
     * This method verifies that the signature matches our secret key, proving the payment wasn't faked by a hacker.
     */
    @Transactional
    public String verifyPayment(String orderId, String paymentId, String signature) {
        try {
            // 1. Recreate the cryptographic signature using our strictly secret key
            String generatedSignature = calculateRFC2104HMAC(orderId + "|" + paymentId, keySecret);

            // 2. If the signature the frontend sent doesn't match ours, someone is trying to cheat the system
            if (!generatedSignature.equals(signature)) { // RazorpayClient also has Utility.verifyPaymentSignature
                throw new RuntimeException("Payment signature verification failed");
            }

            // 3. Mark Booking as successfully Paid since the signature matches mathematically
            Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking order not found"));

            // Save the exact transaction ID for our accounting records
            booking.setRazorpayPaymentId(paymentId);
            booking.setPaymentStatus(PaymentStatus.COMPLETED); // officially paid!
            bookingRepository.save(booking);

            return "Payment Verified Successfully";

        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }

    /**
     * Helper Tool: Cryptographic Signature Generator
     * Uses the HMAC-SHA256 algorithm to create an unforgeable digital signature combining the Order ID and Payment ID.
     */
    public static String calculateRFC2104HMAC(String data, String secret) throws java.security.SignatureException {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new java.security.SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }
}
