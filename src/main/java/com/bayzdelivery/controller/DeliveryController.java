package com.bayzdelivery.controller;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bayzdelivery.dto.TopDeliveryManDTO;
import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.service.DeliveryService;

@RestController
public class DeliveryController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    @Autowired
    DeliveryService deliveryService;

    @PostMapping("/delivery/order")
    public ResponseEntity<Delivery> createNewOrder(@RequestBody Delivery delivery) {
        try {
            Delivery savedOrder = deliveryService.createOrder(delivery);
            log.info("Successfully created new order with ID: {}", savedOrder.getId());
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create new order: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/delivery/complete")
    public ResponseEntity<Delivery> completeDelivery(@RequestBody Delivery delivery) {
        try {
            Delivery completedDelivery = deliveryService.completeDelivery(delivery);
            log.info("Successfully completed delivery for order ID: {}", completedDelivery.getOrderId());
            return ResponseEntity.ok(completedDelivery);
        } catch (Exception e) {
            log.error("Failed to complete delivery: {}", e.getMessage(), e);
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

    @GetMapping("/delivery/top")
    public ResponseEntity<List<TopDeliveryManDTO>> getTopDeliveryMen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        try {
            List<TopDeliveryManDTO> topDeliveryMen = deliveryService.getTopDeliveryMen(startTime, endTime);
            log.info("Found {} top delivery men", topDeliveryMen.size());
            return ResponseEntity.ok(topDeliveryMen);
        } catch (Exception e) {
            log.error("Failed to get top delivery men: {}", e.getMessage(), e);
            throw e;
        }
    }
}