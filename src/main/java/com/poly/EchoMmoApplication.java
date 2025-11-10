package com.poly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Enables Spring Boot auto-configuration and component scanning
public class EchoMmoApplication {

    // Main method - entry point for the application
    public static void main(String[] args) {
        // Launches the Spring Boot application
        SpringApplication.run(EchoMmoApplication.class, args);
    }

}