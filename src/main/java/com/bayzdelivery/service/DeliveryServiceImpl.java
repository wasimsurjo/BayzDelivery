package com.bayzdelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.model.Person;
import com.bayzdelivery.model.PersonRole;
import com.bayzdelivery.repositories.DeliveryRepository;
import com.bayzdelivery.repositories.PersonRepository;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

  @Autowired
  DeliveryRepository deliveryRepository;

  @Autowired
  PersonRepository personRepository;

  @Override
  public Delivery save(Delivery delivery) {
        log.info("Attempting to save new delivery for delivery man: {}", delivery.getDeliveryMan().getId());
        try {
            validateDelivery(delivery);
            calculateCommission(delivery);
            Delivery savedDelivery = deliveryRepository.save(delivery);
            log.info("Successfully saved delivery with ID: {}", savedDelivery.getId());
            return savedDelivery;
        }  catch (Exception e) {
            log.error("Unexpected error while saving delivery: {}", e.getMessage(), e);
            throw e;
        }
    }

  @Override
  public Delivery findById(Long deliveryId) {
    try{
    log.debug("Looking up delivery with ID: {}", deliveryId);
    Optional<Delivery> optionalDelivery = deliveryRepository.findById(deliveryId);
    if (optionalDelivery.isPresent()) {
        log.debug("Found delivery with ID: {}", deliveryId);
    return optionalDelivery.get();
        }
        log.debug("No delivery found with ID: {}", deliveryId);
        return null;
    } catch (Exception e) {
        log.error("Failed to find delivery with ID: {}", deliveryId, e);
        throw e;
    }
    }


    private void calculateCommission(Delivery delivery) {
        try {
            log.debug("Calculating commission for delivery");
            if (delivery.getPrice() != null && delivery.getDistance() != null) {
                BigDecimal price = BigDecimal.valueOf(delivery.getPrice());
                BigDecimal distance = BigDecimal.valueOf(delivery.getDistance());
                
                BigDecimal priceCommission = price.multiply(BigDecimal.valueOf(0.05));
                BigDecimal distanceCommission = distance.multiply(BigDecimal.valueOf(0.5));
                
                BigDecimal totalCommission = priceCommission.add(distanceCommission)
                    .setScale(2, RoundingMode.HALF_UP);
                
                delivery.setCommission(totalCommission.longValue());
                log.debug("Commission calculated: {}", totalCommission);
            }
        } catch (Exception e) {
            log.error("Failed to calculate commission for delivery: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateDelivery(Delivery delivery) {
        try{
        log.debug("Validating delivery for delivery man: {}", delivery.getDeliveryMan().getId());
        
        Instant now = Instant.now();
        if (delivery.getStartTime().isBefore(now)) {
            log.warn("Delivery start time is in the past: {}", delivery.getStartTime());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery cannot start in the past!");
        }
        if (delivery.getEndTime().isBefore(now)) {
            log.warn("Delivery end time is in the past: {}", delivery.getEndTime());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery cannot end in the past!");
        }

        Optional<Person> optionalDeliveryMan = personRepository.findById(delivery.getDeliveryMan().getId());
        if (!optionalDeliveryMan.isPresent()) {
            log.error("Invalid delivery man ID: {}", delivery.getDeliveryMan().getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery man ID invalid or does not exist!");
        }
        Person deliveryMan = optionalDeliveryMan.get();
        if (deliveryMan.getRole() != PersonRole.DELIVERY_MAN) {
            log.error("Person {} is not a delivery man", deliveryMan.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested Person is not a delivery man!");
        }

        boolean hasConcurrentDelivery = ((List<Delivery>)deliveryRepository.findAll()).stream()
            .filter(d -> d.getDeliveryMan().getId().equals(deliveryMan.getId()))
            .anyMatch(d -> {
                boolean startsDuring = delivery.getStartTime().isAfter(d.getStartTime()) && 
                                     delivery.getStartTime().isBefore(d.getEndTime());
                boolean endsDuring = delivery.getEndTime().isAfter(d.getStartTime()) && 
                                   delivery.getEndTime().isBefore(d.getEndTime());
                return startsDuring || endsDuring;
            });
        
        if (hasConcurrentDelivery) {
            log.warn("Concurrent delivery detected for delivery man: {}", deliveryMan.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery man is already on a delivery!");
        }

        Optional<Person> optionalCustomer = personRepository.findById(delivery.getCustomer().getId());
        if (!optionalCustomer.isPresent()) {
            log.error("Invalid customer ID: {}", delivery.getCustomer().getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID invalid or does not exist!");
        }
        Person customer = optionalCustomer.get();
        if (customer.getRole() != PersonRole.CUSTOMER) {
            log.error("Person {} is not a customer", customer.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested Person is not a customer!");
        }

        if (delivery.getEndTime().isBefore(delivery.getStartTime())) {
            log.warn("Invalid delivery times - end before start: start={}, end={}", 
                    delivery.getStartTime(), delivery.getEndTime());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time cannot be before start time!");
        }

        if (delivery.getDistance() != null && delivery.getDistance() <= 0) {
            log.warn("Invalid distance value: {}", delivery.getDistance());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Distance must be a valid value! ");
        }
        if (delivery.getPrice() != null && delivery.getPrice() <= 0) {
            log.warn("Invalid price value: {}", delivery.getPrice());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be a valid value! ");
        }
        
        log.debug("Delivery validation successful");
    } catch (ResponseStatusException e) {
        log.error("Validation failed: {}", e.getMessage(), e);
        throw e;
    } catch (Exception e) {
        log.error("Failed to validate delivery: {}", e.getMessage(), e);
        throw e;
    }
  }
}
