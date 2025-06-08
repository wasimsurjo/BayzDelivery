package com.bayzdelivery.service;

import java.time.Instant;
import java.util.List;
import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.dto.TopDeliveryManDTO;

public interface DeliveryService {

  public Delivery createOrder(Delivery delivery);

  public Delivery completeDelivery(Delivery delivery);

  public Delivery findById(Long deliveryId);

  public List<TopDeliveryManDTO> getTopDeliveryMen(Instant startTime, Instant endTime); 
}
