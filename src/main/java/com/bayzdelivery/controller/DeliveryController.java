package com.bayzdelivery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.service.DeliveryService;

@RestController
public class DeliveryController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    @Autowired
    DeliveryService deliveryService;

    @PostMapping("/delivery")
    public ResponseEntity<Delivery> createNewDelivery(@RequestBody Delivery delivery) {
        try {
            Delivery savedDelivery = deliveryService.save(delivery);
            log.info("Successfully created delivery request for ID: {}", savedDelivery.getId());
            return ResponseEntity.ok(savedDelivery);
        } catch (Exception e) {
            log.error("Failed to create delivery request: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long deliveryId) {
        try{
        Delivery delivery = deliveryService.findById(deliveryId);
        if (delivery != null) {
            log.debug("Found delivery info for ID: {}", deliveryId);
            return ResponseEntity.ok(delivery);
        }
        log.debug("No delivery info found for ID: {}", deliveryId);
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        log.error("Failed to get delivery by ID: {}", deliveryId, e);
        throw e;
        }
    }       
}