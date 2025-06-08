package com.bayzdelivery.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import com.bayzdelivery.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

@Repository
@RestResource(exported = false)
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    @Query("SELECT d FROM Delivery d WHERE d.orderId = :orderId")
    Optional<Delivery> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT d FROM Delivery d " +
           "WHERE d.startTime IS NOT NULL " +
           "AND d.endTime IS NULL " +
           "AND d.startTime <= :thresholdTime")
    List<Delivery> findDelayedDeliveries(@Param("thresholdTime") Instant thresholdTime);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Delivery d " +
           "WHERE d.deliveryMan.id = :deliveryManId " +
           "AND d.orderId != :orderId " +
           "AND d.startTime < :endTime " +
           "AND d.endTime > :startTime")
    boolean hasConcurrentDelivery(
        @Param("deliveryManId") Long deliveryManId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        @Param("orderId") String orderId
    );

    @Query("SELECT d.deliveryMan.id as deliveryManId, d.deliveryMan.name as deliveryManName, " +
           "SUM(d.commission) as totalCommission, COUNT(d) as totalDeliveries " +
           "FROM Delivery d " +
           "WHERE d.startTime >= :startTime AND d.endTime <= :endTime " +
           "GROUP BY d.deliveryMan.id, d.deliveryMan.name " +
           "ORDER BY totalCommission DESC")
    List<Object[]> findTopDeliveryMenByCommission(
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
}
