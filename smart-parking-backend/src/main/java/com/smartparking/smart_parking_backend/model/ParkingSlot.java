package com.smartparking.smart_parking_backend.model;

// Jakarta Persistence API (JPA) annotations for database mapping
import jakarta.persistence.*;
// Utility class for handling lists of items
import java.util.List;

/**
 * ParkingSlot Model Class
 * 
 * Purpose: Represents a physical parking location/slot registered in the system by a property owner.
 * It contains details like location, capacities for different vehicle types, pricing, and the owner.
 * 
 * Key Annotations:
 * - @Entity: Marks this class as a database entity.
 * - @Table: Specifies the table name "parking_slots" in the database.
 */
@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    // Primary Key of the table, auto-incremented by the database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location; // General location description
    private double latitude;  // GPS latitude
    private double longitude; // GPS longitude

    // CAPACITIES: How many vehicles of each type can this slot hold?
    private int carCapacity;
    private int bikeCapacity;
    private int truckCapacity;

    // ENHANCED FIELDS
    private String imageUrl; // URL to an image showing the parking slot
    private String address;  // Detailed street address
    private String city;     // City where the slot is located
    private String reviews;  // JSON or text containing user reviews (for demo purposes)
    private String upiId;    // The owner's UPI ID for receiving payments

    // ==========================================
    // RELATIONSHIPS
    // ==========================================

    /**
     * Many ParkingSlots can be owned by one User (Owner).
     * FetchType.LAZY means the owner details are only loaded from the database when specifically requested, saving memory.
     * @JsonIgnoreProperties prevents infinite loops during JSON serialization when referencing the lazy-loaded owner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User owner;

    /**
     * One ParkingSlot can have many Bookings.
     * @OneToMany marks the one-to-many relationship, linked by the "parkingSlot" field in the Booking class.
     * @JsonIgnore prevents infinite loops when sending the ParkingSlot data to the frontend (it won't send the massive list of bookings).
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "parkingSlot")
    private List<Booking> booking;

    // PRICING: Cost to park per hour for each vehicle type
    private double carPricePerHour;
    private double bikePricePerHour;
    private double truckPricePerHour;

    // Is the slot currently active/visible to users?
    @Column(nullable = false)
    private boolean enabled = true;

    // VERIFICATION STATUS
    // Used by Admins to approve or reject new parking slots.
    // Possible values: PENDING, APPROVED, REJECTED
    private String status = "PENDING";
    
    // Comments left by the admin when approving/rejecting
    private String adminComments;


    // Default constructor needed by JPA
    public ParkingSlot() {
    }

    // ==========================================
    // MANUAL GETTERS AND SETTERS
    // These methods allow other parts of the application to safely read and modify the private fields.
    // ==========================================

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getReviews() { return reviews; }
    public void setReviews(String reviews) { this.reviews = reviews; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getCarCapacity() { return carCapacity; }
    public void setCarCapacity(int carCapacity) { this.carCapacity = carCapacity; }

    public int getBikeCapacity() { return bikeCapacity; }
    public void setBikeCapacity(int bikeCapacity) { this.bikeCapacity = bikeCapacity; }

    public int getTruckCapacity() { return truckCapacity; }
    public void setTruckCapacity(int truckCapacity) { this.truckCapacity = truckCapacity; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Booking> getBooking() { return booking; }
    public void setBooking(List<Booking> booking) { this.booking = booking; }

    public double getCarPricePerHour() { return carPricePerHour; }
    public void setCarPricePerHour(double carPricePerHour) { this.carPricePerHour = carPricePerHour; }

    public double getBikePricePerHour() { return bikePricePerHour; }
    public void setBikePricePerHour(double bikePricePerHour) { this.bikePricePerHour = bikePricePerHour; }

    public double getTruckPricePerHour() { return truckPricePerHour; }
    public void setTruckPricePerHour(double truckPricePerHour) { this.truckPricePerHour = truckPricePerHour; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
