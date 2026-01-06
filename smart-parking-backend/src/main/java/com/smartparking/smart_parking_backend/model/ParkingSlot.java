package com.smartparking.smart_parking_backend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private double latitude;
    private double longitude;

    // CAPACITIES
    private int carCapacity;
    private int bikeCapacity;
    private int truckCapacity;

    // ENHANCED FIELDS
    private String imageUrl; // URL to slot image
    private String address; // Street address
    private String city; // City name
    private String reviews; // JSON or plain text reviews (for demo)
    private String upiId; // Owner's UPI ID for payments

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User owner;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "parkingSlot")
    private List<Booking> booking;

    // PRICING
    private double carPricePerHour;
    private double bikePricePerHour;
    private double truckPricePerHour;

    @Column(nullable = false)
    private boolean enabled = true;

    // VERIFICATION STATUS
    // PENDING, APPROVED, REJECTED
    private String status = "PENDING";
    private String adminComments;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }

    public ParkingSlot() {
    }

    // MANUAL GETTERS AND SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCarCapacity() {
        return carCapacity;
    }

    public void setCarCapacity(int carCapacity) {
        this.carCapacity = carCapacity;
    }

    public int getBikeCapacity() {
        return bikeCapacity;
    }

    public void setBikeCapacity(int bikeCapacity) {
        this.bikeCapacity = bikeCapacity;
    }

    public int getTruckCapacity() {
        return truckCapacity;
    }

    public void setTruckCapacity(int truckCapacity) {
        this.truckCapacity = truckCapacity;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Booking> getBooking() {
        return booking;
    }

    public void setBooking(List<Booking> booking) {
        this.booking = booking;
    }

    public double getCarPricePerHour() {
        return carPricePerHour;
    }

    public void setCarPricePerHour(double carPricePerHour) {
        this.carPricePerHour = carPricePerHour;
    }

    public double getBikePricePerHour() {
        return bikePricePerHour;
    }

    public void setBikePricePerHour(double bikePricePerHour) {
        this.bikePricePerHour = bikePricePerHour;
    }

    public double getTruckPricePerHour() {
        return truckPricePerHour;
    }

    public void setTruckPricePerHour(double truckPricePerHour) {
        this.truckPricePerHour = truckPricePerHour;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
