package com.paylite.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the wallet-service Spring Boot application.
 *
 * The @SpringBootApplication annotation is a shortcut for three things at once:
 *   1. @Configuration         - this class can declare Spring beans
 *   2. @EnableAutoConfiguration - Spring scans the classpath and auto-wires
 *      sensible defaults (e.g., sees the PostgreSQL driver -> sets up DataSource)
 *   3. @ComponentScan         - scans this package and below for @Component,
 *      @Service, @Repository, @RestController classes and registers them
 */
@SpringBootApplication
public class WalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }
}