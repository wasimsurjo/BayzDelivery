package com.bayzdelivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BayzDeliveryApplication {
    private static final Logger log = LoggerFactory.getLogger(BayzDeliveryApplication.class);

    public static void main(String[] args) {
        try{
        log.info("Starting Bayz Delivery API.................");
        SpringApplication.run(BayzDeliveryApplication.class, args);
        log.info("Bayz Delivery API started successfully........");
    } catch (Exception e) {
        log.error("Failed to start Bayz Delivery API: {}", e.getMessage(), e);
        throw e;
    }
    }
}
