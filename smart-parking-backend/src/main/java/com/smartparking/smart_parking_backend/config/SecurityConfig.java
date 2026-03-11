package com.smartparking.smart_parking_backend.config;

import com.smartparking.smart_parking_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig Class
 * 
 * Purpose: This is the central command center for all Spring Security rules.
 * It strictly dictates WHAT APIs are public (anyone can use them without logging in)
 * and WHAT APIs are private (require a valid JWT token / specific role).
 * 
 * Key Annotations:
 * - @Configuration: Marks this as a settings file that Spring Boot must read on startup.
 * - @EnableWebSecurity: Completely turns on custom web security, overriding Spring's defaults.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Dependency Injection referencing our custom JWT Bouncer
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Creates a tool that securely encrypts passwords.
     * We use BCrypt. When a user creates the password "1234", BCrypt turns it into 
     * a massive, unreadable hash (e.g., "$2a$10$xyz...") before storing it in the database.
     * 
     * The @Bean annotation tells Spring: "Create this once and let anyone who needs it borrow it."
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The Main Security Filter Chain.
     * This defines the exact pipeline that every single HTTP request must pass through.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS Configuration: Tells the browser which frontend websites are allowed to talk to this backend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 2. CSRF (Cross-Site Request Forgery): Since we use JWT tokens instead of browser cookies, 
                // we don't need CSRF protection. Disabling it stops errors with simple API requests.
                .csrf(csrf -> csrf.disable())

                // 3. The rule book for our API routes (URLs)
                .authorizeHttpRequests(auth -> auth
                        // 🟢 PUBLIC ROUTES: Anyone can access these (no login needed)
                        .requestMatchers(
                                "/api/users/signup", // Creating a new account
                                "/api/users/login",  // Logging in
                                "/api/slots/map",    // Viewing the public map
                                "/api/slots/*/availability") // Checking if a slot has space
                        .permitAll()
                        
                        // 🔴 ADMIN ONLY ROUTES: Only users with ROLE_ADMIN can access URLs starting with /api/admin/
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // 🟡 PRIVATE ROUTES: Every single other URL in the entire backend REQUIRES the user to be logged in with a valid JWT.
                        .anyRequest().authenticated())

                // 4. Inject our custom JWT Security Filter.
                // We tell Spring: "Put our custom JWT bouncer exactly in front of your standard Username/Password bouncer."
                // This forces standard API requests to be checked for a JWT token first!
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // 5. Disable default Spring Security forms
                // We are building a REST API for React/Mobile apps, so we don't want Spring generating ugly HTML login pages
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Detailed CORS Rules.
     * Cross-Origin Resource Sharing (CORS) is a browser security feature.
     * Overcoming CORS errors is historically the hardest part of connecting a frontend to a backend!
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow ANY website to connect to us (Use specific URLs in production, e.g., "http://localhost:3000")
        configuration.setAllowedOriginPatterns(List.of("*")); 
        
        // Allow standard HTTP actions (Create, Read, Update, Delete)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow the frontend to send custom headers (like "Authorization: Bearer <token>")
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow the frontend to attach credentials like cookies or auth headers securely
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply these generous rules to ALL paths ("/**") on our backend
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
