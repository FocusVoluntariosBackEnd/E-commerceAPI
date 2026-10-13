package com.ecommerce.ecommerce_api.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest (
        @NotNull
        UUID productId,

        @NotNull
        @Positive
        Integer quantity
) {
}
