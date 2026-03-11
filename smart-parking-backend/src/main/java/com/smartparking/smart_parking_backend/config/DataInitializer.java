package com.smartparking.smart_parking_backend.config;

import com.smartparking.smart_parking_backend.model.Role;
import com.smartparking.smart_parking_backend.model.User;
import com.smartparking.smart_parking_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * DataInitializer Class
 * 
 * Purpose: This acts as a bootstrap script that runs ONE TIME immediately after the 
 * Spring Boot application starts up, but before it starts accepting web traffic.
 * 
 * Specifically, it performs a critical safety check: Does a master "Admin" account exist 
 * in the database? If not, it creates one automatically. This guarantees the system 
 * always has at least one administrator to approve parking slots and manage users.
 */
@Configuration
public class DataInitializer {

    /**
     * CommandLineRunner is an interface Spring Boot looks for on startup.
     */
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check the database: Do we already have the default admin email?
            if (userRepository.findByEmail("admin@smartparking.com").isEmpty()) {
                
                // If the email is missing, we assemble a brand new Admin user
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("admin@smartparking.com");
                
                // CRUCIAL: Never save passwords in plain text! We use the assigned BCrypt encoder first.
                admin.setPassword(passwordEncoder.encode("admin123"));
                
                // Give them the ultimate authority
                admin.setRole(Role.ADMIN);
                admin.setPhone("0000000000");
                
                // Save them into the PostgreSQL database permanently
                userRepository.save(admin);
                
                // Print a helpful message to the server terminal so the developer knows the credentials
                System.out.println("ADMIN USER CREATED: admin@smartparking.com / admin123");
            }
        };
    }
}
