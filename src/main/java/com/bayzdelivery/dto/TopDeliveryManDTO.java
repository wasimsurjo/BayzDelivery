package com.bayzdelivery.dto;

import java.math.BigDecimal;

public class TopDeliveryManDTO {
    private Long deliveryManId;
    private String deliveryManName;
    private BigDecimal totalCommission;
    private Long totalDeliveries;

    public TopDeliveryManDTO(Long deliveryManId, String deliveryManName, BigDecimal totalCommission, Long totalDeliveries) {
        this.deliveryManId = deliveryManId;
        this.deliveryManName = deliveryManName;
        this.totalCommission = totalCommission;
        this.totalDeliveries = totalDeliveries;
    }

    public Long getDeliveryManId() {
        return deliveryManId;
    }

    public String getDeliveryManName() {
        return deliveryManName;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public Long getTotalDeliveries() {
        return totalDeliveries;
    }
} 