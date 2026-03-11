package com.smartparking.smart_parking_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter Class
 * 
 * Purpose: This is a "Filter" that intercepts EVERY SINGLE HTTP request coming into the backend
 * BEFORE the request reaches your Controllers. 
 * 
 * Its job is to act like a bouncer at a club:
 * 1. Checks if the user brought their ID card (JWT Token in the Header).
 * 2. Checks if the ID card is real and hasn't expired.
 * 3. Reads the name and role from the ID card.
 * 4. Tells the rest of the app "Hey, this is John, and he's an OWNER. Let him in."
 * 
 * Key Annotation:
 * - @Component: Tells Spring Boot to automatically detect and load this filter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Dependency Injection
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * The core logic that runs exactly once per incoming request.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Look for the "Authorization" header in the incoming HTTP request.
        // This is where standard frontend apps put the JWT token.
        String authHeader = request.getHeader("Authorization");

        // 2. If there's no header, or it doesn't start with "Bearer ", the user isn't logged in.
        // We let the request continue anyway (maybe they are trying to access a public login page).
        // If they try to access a private page, Spring Security will block them later.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract just the token part (removing the "Bearer " prefix, which is 7 characters)
        String token = authHeader.substring(7);
        
        // 4. Use our JwtService helper to decode the token and get the user's details
        String username = jwtService.extractUsername(token); // Usually their email
        String role = jwtService.extractRole(token); // e.g., "CUSTOMER", "OWNER", "ADMIN"

        // 5. If we found a username AND the current security context is empty (meaning they aren't authenticated yet in this specific request cycle)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Convert our simple string role (e.g., "ADMIN") into Spring Security's official format ("ROLE_ADMIN")
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            // Create an official Spring Security authentication token proving who the user is and what power they have
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null, // We don't need their password here, the JWT token itself proves they already logged in successfully before
                            List.of(authority)
                    );

            // 6. VERY IMPORTANT: Save this authentication info into the "SecurityContextHolder".
            // Think of this as putting a temporary VIP wristband on the user for the duration of this one request.
            // Any Service class anywhere in the app can now use SecurityContextHolder.getContext().getAuthentication().getName() to see who is logged in!
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. Pass the request down the chain to the next filter, or finally to the Controller that handles the specific API path.
        filterChain.doFilter(request, response);
    }
}
