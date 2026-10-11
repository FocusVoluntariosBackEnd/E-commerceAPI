package com.ecommerce.ecommerce_api.cart.dto;

import com.ecommerce.ecommerce_api.cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}