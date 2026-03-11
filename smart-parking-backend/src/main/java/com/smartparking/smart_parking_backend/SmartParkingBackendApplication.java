// Package declaration defined the namespace. 
// Spring Boot uses the root package (com.smartparking.smart_parking_backend) 
// for "component scanning" - automatically finding all your Controllers, Services, etc.
package com.smartparking.smart_parking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartParkingBackendApplication Class
 * 
 * Purpose: This is the absolute starting point of your entire backend application.
 * When you hit "Run" or execute `mvn spring-boot:run`, this is the file that wakes up first.
 */
@SpringBootApplication // This single annotation does 3 massive things:
// 1. @Configuration: Tags the class as a source of bean definitions for the application context.
// 2. @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
// 3. @ComponentScan: Tells Spring to look for other components, configurations, and services in the 'com.smartparking.smart_parking_backend' package, allowing it to find the controllers.
public class SmartParkingBackendApplication {

	// The standard Java main method - the entry point of the program.
	public static void main(String[] args) {
		// This line actually launches the Spring application, starting the internal web server (Tomcat)
		// and loading the Spring application context.
		SpringApplication.run(SmartParkingBackendApplication.class, args);
	}

}
