package com.smartparking.smart_parking_backend.model;

// Jakarta Persistence API (JPA) annotations for mapping Java objects to database tables
import jakarta.persistence.*;
// LocalDateTime is used to handle date and time without timezones
import java.time.LocalDateTime;

/**
 * Booking Model Class
 * 
 * Purpose: This class represents a "Booking" entity in the database.
 * It stores all the information related to a parking slot reservation made by a user for a specific vehicle.
 * 
 * Key Annotations:
 * - @Entity: Tells Spring/Hibernate that this class maps to a database table.
 * - @Table: Specifies the exact name of the table in the database ("bookings").
 */
@Entity
@Table(name = "bookings")
public class Booking {

    // Primary Key of the table
    // @Id marks this field as the primary key.
    // @GeneratedValue means the database will automatically generate this ID (like 1, 2, 3...) when a new booking is created.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many bookings can belong to one User.
    // @JoinColumn specifies the foreign key column name in the "bookings" table.
    // nullable = false means a booking must always have a user associated with it.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many bookings can be made for one ParkingSlot over time.
    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkingSlot parkingSlot;

    // Many bookings can be made for one Vehicle over time.
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Booking timestamps
    private LocalDateTime startTime; // When the parking starts
    private LocalDateTime endTime;   // When the parking ends
    private boolean active;          // Is the booking currently active?

    // Enum to track payment status (e.g., PENDING, COMPLETED, FAILED)
    // @Enumerated(EnumType.STRING) saves the enum name as text in the database instead of a number.
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING; // Default to PENDING

    private double totalPrice; // Total cost of the booking
    private LocalDateTime createdAt; // Record creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp

    private String paymentReference; // Stores UTR or Transaction ID from UPI apps

    // RAZORPAY FIELDS (Used if payment Gateway integration is active)
    private String razorpayOrderId;
    private String razorpayPaymentId;

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    /**
     * @PrePersist is a JPA callback.
     * This method runs automatically right BEFORE a new booking is saved to the database for the very first time.
     * It sets the initial creation time, update time, and marks the booking as active.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * @PreUpdate is a JPA callback.
     * This method runs automatically right BEFORE an existing booking is updated in the database.
     * It ensures the 'updatedAt' timestamp is always fresh.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // MANUAL GETTERS AND SETTERS
    // These methods allow other parts of the application to safely read (get) and modify (set) the private fields above.
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ParkingSlot getParkingSlot() { return parkingSlot; }
    public void setParkingSlot(ParkingSlot parkingSlot) { this.parkingSlot = parkingSlot; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}