package com.smartparking.smart_parking_backend.model;

// Imports JPA annotations for handling database connections 
import jakarta.persistence.*;

/**
 * User Model Class
 * 
 * Purpose: Serves as the central entity for all individuals interacting with the system.
 * This class stores profile information, credentials, and the assigned role (USER, OWNER, ADMIN).
 * 
 * Key Annotations:
 * - @Entity: Maps the Java class to a database table.
 * - @Table: Specifies that this maps to the "users" table.
 */
@Entity
@Table(name = "users")
public class User {

    // Unique identifier for the user (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // changed from long to Long for consistency

    // Basic user information
    private String name;

    // Email is used as the login username.
    // @Column(unique = true) ensures no two users can register with the same email.
    // nullable = false ensures an email must be provided to create an account.
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * One User (specifically an OWNER) can have many ParkingSlots.
     * mappedBy = "owner" points back to the "owner" field in the ParkingSlot class, making this a bidirectional relationship.
     * @JsonIgnore stops infinite loops where a User fetches their slots, and each slot fetches the User, over and over.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "owner")
    private java.util.List<ParkingSlot> parkingSlots;

    public java.util.List<ParkingSlot> getParkingSlots() {
        return parkingSlots;
    }

    public void setParkingSlots(java.util.List<ParkingSlot> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }

    // Encrypted password for login auth
    @Column(nullable = false)
    private String password;

    // The role dictates what permissions the user has (USER, OWNER, ADMIN).
    // @Enumerated(EnumType.STRING) stores the word "ADMIN" rather than a confusing number in the database.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Extended User profile fields
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // Used by 'OWNER' roles to receive payouts
    @Column(name = "upi_id")
    private String upiId;

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    /**
     * @PrePersist is a JPA lifecycle callback.
     * Right BEFORE saving a brand new user to the database, this checks if a role was explicitly set.
     * If not, it defaults them to a standard "USER" to prevent errors and ensure security.
     */
    @PrePersist
    protected void onCreate() {
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    // Default constructor needed by JPA
    public User() {
    }

    // ==========================================
    // MANUAL GETTERS AND SETTERS
    // Standard methods to read and write to the object's private properties
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}