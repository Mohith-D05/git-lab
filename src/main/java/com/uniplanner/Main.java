package com.uniplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ========================================================================
 * UniPlanner - Intelligent Multi-Section Timetable Management System
 * ========================================================================
 * Single entry point for the Spring Boot application.
 *
 * @SpringBootApplication combines:
 * - @Configuration: Marks class as source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot auto-configuration
 * - @ComponentScan: Scans com.uniplanner package for @Service, @Component, @RestController
 * ========================================================================
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("\n=================================================");
        System.out.println("  UniPlanner is running at http://localhost:8082");
        System.out.println("  API base: http://localhost:8082/api");
        System.out.println("=================================================\n");
    }
}
