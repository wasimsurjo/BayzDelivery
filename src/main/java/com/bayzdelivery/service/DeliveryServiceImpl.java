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
import com.bayzdelivery.dto.TopDeliveryManDTO;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    PersonRepository personRepository;

    @Override
    public Delivery createOrder(Delivery delivery) {
        log.info("Creating new order for customer: {}", delivery.getCustomer().getId());
        try {
            validateCustomer(delivery.getCustomer().getId());
            validateDeliveryMan(delivery.getDeliveryMan().getId());
            validateStartTime(delivery.getStartTime());
            delivery.setEndTime(null);
            delivery.setDistance(null);
            delivery.setCommission(null);
            Delivery savedOrder = deliveryRepository.save(delivery);
            log.info("Successfully created order with ID: {}", savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Delivery completeDelivery(Delivery delivery) {
        log.info("Completing delivery for order: {}", delivery.getOrderId());
        try {
            Optional<Delivery> existingOrder = deliveryRepository.findByOrderId(delivery.getOrderId());
            if (!existingOrder.isPresent()) {
                log.error("Order not found: {}", delivery.getOrderId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found!");
            }

            Delivery orderToUpdate = existingOrder.get();
            
            if (!orderToUpdate.getDeliveryMan().getId().equals(delivery.getDeliveryMan().getId())) {
                log.error("Delivery man mismatch for order: {}. Expected: {}, Got: {}", 
                    delivery.getOrderId(), 
                    orderToUpdate.getDeliveryMan().getId(), 
                    delivery.getDeliveryMan().getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only the assigned delivery man can complete this order!");
            }
            
            orderToUpdate.setStartTime(delivery.getStartTime());
            orderToUpdate.setEndTime(delivery.getEndTime());
            orderToUpdate.setDistance(delivery.getDistance());
            orderToUpdate.setPrice(delivery.getPrice());

            validateDelivery(orderToUpdate);
            calculateCommission(orderToUpdate);
            
            Delivery completedDelivery = deliveryRepository.save(orderToUpdate);
            log.info("Successfully completed delivery for order ID: {}", completedDelivery.getOrderId());
            return completedDelivery;
        } catch (Exception e) {
            log.error("Failed to complete delivery: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateCustomer(Long customerId) {
        Optional<Person> optionalCustomer = personRepository.findById(customerId);
        if (!optionalCustomer.isPresent()) {
            log.error("Invalid customer ID: {}", customerId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID invalid or does not exist!");
        }
        Person customer = optionalCustomer.get();
        if (customer.getRole() != PersonRole.CUSTOMER) {
            log.error("Person {} is not a customer", customer.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested Person is not a customer!");
        }
    }

    private void validateDelivery(Delivery delivery) {
        try {
            log.debug("Validating delivery for order: {}", delivery.getOrderId());
            
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

            boolean hasConcurrentDelivery = deliveryRepository.hasConcurrentDelivery(
                deliveryMan.getId(),
                delivery.getStartTime(),
                delivery.getEndTime(),
                delivery.getOrderId()
            );
            
            if (hasConcurrentDelivery) {
                log.warn("Concurrent delivery detected for delivery man: {}", deliveryMan.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery man is already on a delivery!");
            }

            validateCustomer(delivery.getCustomer().getId());

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

    private void calculateCommission(Delivery delivery) {
        if (delivery.getPrice() != null && delivery.getDistance() != null) {
            BigDecimal priceCommission = BigDecimal.valueOf(delivery.getPrice()).multiply(BigDecimal.valueOf(0.05));
            BigDecimal distanceCommission = BigDecimal.valueOf(delivery.getDistance()).multiply(BigDecimal.valueOf(0.5));
            BigDecimal totalCommission = priceCommission.add(distanceCommission).setScale(0, RoundingMode.HALF_UP);
            delivery.setCommission(totalCommission.longValue());
            log.debug("Calculated commission: {} for delivery", totalCommission);
        }
    }

    @Override
    public Delivery findById(Long deliveryId) {
        try {
            log.debug("Looking up delivery with ID: {}", deliveryId);
            Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
            if (delivery.isPresent()) {
                log.debug("Found delivery with ID: {}", deliveryId);
                return delivery.get();
            }
            log.debug("No delivery found with ID: {}", deliveryId);
            return null;
        } catch (Exception e) {
            log.error("Failed to find delivery with ID: {}", deliveryId, e);
            throw e;
        }
    }

    @Override
    public List<TopDeliveryManDTO> getTopDeliveryMen(Instant startTime, Instant endTime) {
        log.info("Fetching top delivery men from {} to {}", startTime, endTime);
        try {
            List<Object[]> results = deliveryRepository.findTopDeliveryMenByCommission(startTime, endTime);
            List<TopDeliveryManDTO> topDeliveryMen = results.stream()
                .limit(3)
                .map(result -> new TopDeliveryManDTO(
                    (Long) result[0],
                    (String) result[1],
                    new BigDecimal(result[2].toString()),
                    (Long) result[3],
                    null  ))
                .toList();

            if (!topDeliveryMen.isEmpty()) {
                BigDecimal totalCommission = topDeliveryMen.stream()
                    .map(TopDeliveryManDTO::getTotalCommission)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal averageCommission = totalCommission.divide(
                    BigDecimal.valueOf(topDeliveryMen.size()), 
                    2, 
                    RoundingMode.HALF_UP
                );

                topDeliveryMen.forEach(dto -> dto.setAverageCommission(averageCommission));
            }

            return topDeliveryMen;
        } catch (Exception e) {
            log.error("Failed to fetch top delivery men: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateStartTime(Instant startTime) {
        if (startTime == null) {
            log.error("Start time is required for order creation");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time is required!");
        }
        if (startTime.isBefore(Instant.now())) {
            log.error("Start time cannot be in the past: {}", startTime);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time cannot be in the past!");
        }
    }

    private void validateDeliveryMan(Long deliveryManId) {
        Optional<Person> optionalDeliveryMan = personRepository.findById(deliveryManId);
        if (!optionalDeliveryMan.isPresent()) {
            log.error("Invalid delivery man ID: {}", deliveryManId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery man ID invalid or does not exist!");
        }
        Person deliveryMan = optionalDeliveryMan.get();
        if (deliveryMan.getRole() != PersonRole.DELIVERY_MAN) {
            log.error("Person {} is not a delivery man", deliveryMan.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested Person is not a delivery man!");
        }
    }
}
