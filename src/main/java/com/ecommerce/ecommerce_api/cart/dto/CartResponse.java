package com.ecommerce.ecommerce_api.cart.dto;

import com.ecommerce.ecommerce_api.cart.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal total
) {
    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::toResponse)
                .toList();

        return new CartResponse(items, cart.getTotal());
    }
}