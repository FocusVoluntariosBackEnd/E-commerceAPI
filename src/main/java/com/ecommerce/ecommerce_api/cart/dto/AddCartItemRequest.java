package com.ecommerce.ecommerce_api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest (
        @Schema(description = "ID de um produto ativo")
        @NotNull
        UUID productId,

        @Schema(example = "2", description = "Maior que 0")
        @NotNull
        @Positive
        Integer quantity
) {
}