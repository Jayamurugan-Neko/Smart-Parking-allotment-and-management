package com.smartparking.smart_parking_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtService Class
 * 
 * Purpose: A utility service responsible for all cryptographic operations related to JSON Web Tokens (JWT).
 * It handles generating new secure tokens when users log in, and extracting information (claims) 
 * from existing tokens when users make requests.
 */
@Service
public class JwtService {

    // IMPORTANT: In a real production environment, this key MUST be stored securely 
    // in application.properties or environment variables, NOT hardcoded in the file!
    // It must be long and complex to prevent hackers from forging fake tokens.
    private static final String SECRET_KEY =
            "THIS_IS_A_VERY_SECURE_SECRET_KEY_FOR_JWT_256_BITS";

    // Defines how long a token is valid before the user is forced to log in again.
    // Math: 1000 milliseconds * 60 seconds * 60 minutes * 24 hours = 24 hours total.
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; 

    /**
     * Converts our plain text secret key into a cryptographic Key object
     * specifically formatted for the HMAC-SHA256 algorithm.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // -----------------------------
    // Generate Token with ROLE
    // -----------------------------
    
    /**
     * Creates a brand new JWT token for a user who just logged in successfully.
     * 
     * @param username The user's email address (acts as the unique subject).
     * @param role The user's role (e.g., CUSTOMER, OWNER, ADMIN).
     * @return A Base64 string representing the signed JWT block.
     */
    public String generateToken(String username, String role) {

        // "Claims" are just custom pieces of data we want to securely bundle inside the token.
        // Here, we embed the user's role so the frontend knows what screens to show them,
        // and the backend knows what permissions they have without querying the DB every time.
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims) // Add our custom map (the role)
                .setSubject(username) // The primary user identifier (usually email)
                .setIssuedAt(new Date()) // The exact millisecond the token was created
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // When the token dies
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Cryptographically sign it so it can't be tampered with
                .compact(); // Compress it all down into a single URL-safe string
    }

    // -----------------
    // Extract Username
    // -----------------
    
    /**
     * Reads a JWT token and gets the "Subject" (the username/email).
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // -------------
    // Extract Role
    // -------------
    
    /**
     * Reads a JWT token and gets the custom "role" claim we added during generation.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * The core extraction method. It takes a token string, verifies the cryptographic signature
     * using our secret key, and decrypts the payload body into a usable Claims object.
     * 
     * NOTE: If the token has expired, or the signature doesn't match our secret key,
     * this method will automatically throw an exception, protecting the app from forged tokens!
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
