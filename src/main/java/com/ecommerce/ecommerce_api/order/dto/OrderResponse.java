package com.ecommerce.ecommerce_api.order.dto;

import com.ecommerce.ecommerce_api.order.entity.Order;
import com.ecommerce.ecommerce_api.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt
) {
    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}