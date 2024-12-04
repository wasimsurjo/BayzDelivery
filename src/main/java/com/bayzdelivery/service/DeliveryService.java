package com.bayzdelivery.service;

import com.bayzdelivery.model.Delivery;

public interface DeliveryService {

  public Delivery save(Delivery delivery);

  public Delivery findById(Long deliveryId);
}
