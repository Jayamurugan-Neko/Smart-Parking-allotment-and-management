package com.smartparking.smart_parking_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig Class
 * 
 * Purpose: A global configuration for Cross-Origin Resource Sharing (CORS).
 * 
 * IMPORTANT DEVELOPER NOTE:
 * This class attempts to allow any frontend applications (React, Angular, etc.) 
 * to talk to this Spring Boot backend globally.
 * 
 * HOWEVER, because `SecurityConfig.java` also defines a `corsConfigurationSource()` Bean,
 * Spring Security's CORS settings often override these standard Web MVC CORS settings.
 * It is best practice to manage CORS purely in `SecurityConfig.java` when using Spring Security
 * to prevent confusing behavior. This class is currently mostly redundant.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@org.springframework.lang.NonNull CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all API paths
                        .allowedOriginPatterns("*") // Allow all frontend URLs
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
