package com.bayzdelivery.jobs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.repositories.DeliveryRepository;

@Component
public class DelayedDeliveryNotifier {

    private static final Logger log = LoggerFactory.getLogger(DelayedDeliveryNotifier.class);
    private static final int DELIVERY_TIME_THRESHOLD_MINUTES = 45;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DeliveryRepository deliveryRepository;
    

    @Scheduled(fixedDelay = 30000)
    public void checkDelayedDeliveries() {
        try {            
            Instant thresholdTime = Instant.now().minus(DELIVERY_TIME_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
            String thresholdTimeStr = LocalDateTime.ofInstant(thresholdTime, ZoneId.systemDefault()).format(formatter);
            log.info("Checking for deliveries started before: {}", thresholdTimeStr);
            
            List<Delivery> delayedDeliveries = deliveryRepository.findDelayedDeliveries(thresholdTime);
            
            if (!delayedDeliveries.isEmpty()) {
                log.warn("Found {} delayed deliveries:", delayedDeliveries.size());
                    
                for (Delivery delivery : delayedDeliveries) {
                    notifyCustomerSupport(delivery);
                }
            } else {
                log.info("All Good! No delayed deliveries found!");
            }
        } catch (Exception e) {
            log.error("Error checking delayed deliveries: {}", e.getMessage(), e);
        }
    }

    @Async
    public void notifyCustomerSupport(Delivery delivery) {
        try {
            long minutesExceeded = ChronoUnit.MINUTES.between(
                delivery.getStartTime(), 
                Instant.now()
            );

            String message = String.format(
                "\n=== DELAYED DELIVERY ALERT ===\n" +
                "Delivery ID: %s\n" +
                "Order ID: %s\n" +
                "Time Exceeded: %d minutes\n" +
                "Delivery Man: %s (ID: %d)\n" +
                "Customer: %s (ID: %d)\n" +
                "Started at: %s\n" +
                "========================",
                delivery.getId(),
                delivery.getOrderId(),
                minutesExceeded,
                delivery.getDeliveryMan().getName(),
                delivery.getDeliveryMan().getId(),
                delivery.getCustomer().getName(),
                delivery.getCustomer().getId(),
                LocalDateTime.ofInstant(delivery.getStartTime(), ZoneId.systemDefault()).format(formatter)
            );
            
            log.warn(message);
        } catch (Exception e) {
            log.error("Error notifying customer support for delivery {}: {}", 
                     delivery.getId(), e.getMessage(), e);
        }
    }
}
