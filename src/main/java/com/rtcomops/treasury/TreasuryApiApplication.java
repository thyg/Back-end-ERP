package com.rtcomops.treasury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Treasury API Application - Main Entry Point.
 * 
 * <p>This is the main class for the Treasury module backend API.
 * It bootstraps the Spring Boot application with all necessary configurations.</p>
 * 
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@SpringBootApplication
public class TreasuryApiApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TreasuryApiApplication.class, args);
    }
}