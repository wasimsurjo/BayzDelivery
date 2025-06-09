package com.bayzdelivery.dto;

import java.math.BigDecimal;

public class TopDeliveryManDTO {
    private Long deliveryManId;
    private String deliveryManName;
    private BigDecimal totalCommission;
    private Long totalDeliveries;
    private BigDecimal averageCommission;

    public TopDeliveryManDTO(Long deliveryManId, String deliveryManName, BigDecimal totalCommission, Long totalDeliveries, BigDecimal averageCommission) {
        this.deliveryManId = deliveryManId;
        this.deliveryManName = deliveryManName;
        this.totalCommission = totalCommission;
        this.totalDeliveries = totalDeliveries;
        this.averageCommission = averageCommission;
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

    public BigDecimal getAverageCommission() {
        return averageCommission;
    }

    public void setAverageCommission(BigDecimal averageCommission) {
        this.averageCommission = averageCommission;
    }
} 