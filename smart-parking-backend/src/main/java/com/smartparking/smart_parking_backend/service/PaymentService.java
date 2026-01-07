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

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final BookingRepository bookingRepository;

    public PaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public String createOrder(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            // Amount in paise (multiply by 100). Minimum amount for Razorpay is 100 paise
            // (₹1).
            int amountInPaise = (int) (booking.getTotalPrice() * 100);
            if (amountInPaise < 100) {
                amountInPaise = 100; // Enforce minimum ₹1
            }
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + bookingId);

            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");

            // Save order ID to our DB for later verification
            booking.setRazorpayOrderId(orderId);
            bookingRepository.save(booking);

            return order.toString(); // Returns full JSON order object

        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay error: " + e.getMessage());
        }
    }

    @Transactional
    public String verifyPayment(String orderId, String paymentId, String signature) {
        try {
            // 1. Verify Signature
            String generatedSignature = calculateRFC2104HMAC(orderId + "|" + paymentId, keySecret);

            if (!generatedSignature.equals(signature)) { // RazorpayClient also has Utility.verifyPaymentSignature
                throw new RuntimeException("Payment signature verification failed");
            }

            // 2. Mark Booking as Paid
            Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking order not found"));

            booking.setRazorpayPaymentId(paymentId);
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking);

            return "Payment Verified Successfully";

        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }

    // Helper for manual verification (or use Razorpay's Utils)
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
