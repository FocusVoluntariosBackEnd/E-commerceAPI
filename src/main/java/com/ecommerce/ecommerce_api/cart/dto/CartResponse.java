package com.ecommerce.ecommerce_api.cart.dto;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        @Schema(example = "7000.00") BigDecimal total
) {
    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::toResponse)
                .toList();

        return new CartResponse(items, cart.getTotal());
    }
}