package com.ecommerce.ecommerce_api.cart.dto;

import com.ecommerce.ecommerce_api.cart.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        @Schema(example = "Notebook") String name,
        @Schema(example = "2") int quantity,
        @Schema(example = "3500.00") BigDecimal unitPrice,
        @Schema(example = "7000.00") BigDecimal subtotal
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