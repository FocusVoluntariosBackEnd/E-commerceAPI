package com.ecommerce.ecommerce_api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(
        @Schema(example = "3", description = "Nova quantidade (maior que 0, até o estoque)")
        @NotNull
        @Positive
        Integer quantity
) {
}