package com.smartparking.smart_parking_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtil Class
 * 
 * Purpose: A static utility class for generating and extracting data from JSON Web Tokens.
 * 
 * IMPORTANT DEVELOPER NOTE: 
 * This class uses a different secret key ("smartparking_super_secure...") than the `JwtService` class.
 * It also uses "userId" as the subject instead of "username/email", and does not store roles.
 * 
 * Having both JwtUtil (static) and JwtService (@Service) is usually redundant. 
 * Consider standardizing your app to only use `JwtService` to prevent conflicting token logic.
 */
public class JwtUtil {

    // 🔐 MUST be at least 256 bits for the HS256 algorithm to work securely.
    // Warning: Hardcoded keys should be moved to application.properties in production!
    private static final Key SECRET_KEY =
            Keys.hmacShaKeyFor(
                    "smartparking_super_secure_secret_key_123456".getBytes()
            );

    // Determines token lifespan: 1000ms * 60s * 60m * 24h = 24 hours.
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; 

    // -------------------------------
    // Generate JWT
    // -------------------------------
    
    /**
     * Creates a very simple JWT token using only the User's Database ID.
     */
    public static String generateToken(Long userId) {

        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // Uses numerical ID instead of email string
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------------------
    // Extract userId from JWT
    // -------------------------------
    
    /**
     * Reads a token created by this specific class and extracts the numeric User ID.
     * It will crash if the token was created by `JwtService`, because the secret keys don't match!
     */
    public static Long extractUserId(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token) // Validates signature and expiration
                .getBody();

        return Long.parseLong(claims.getSubject());
    }
}
