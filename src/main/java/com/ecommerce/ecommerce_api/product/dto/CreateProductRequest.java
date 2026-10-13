package com.ecommerce.ecommerce_api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @Schema(example = "Notebook")
        @NotBlank
        String name,

        @Schema(example = "Notebook para estudos")
        String description,

        @Schema(example = "3500.00", description = "Maior ou igual a 0")
        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @Schema(example = "10", description = "Maior ou igual a 0")
        @NotNull
        @PositiveOrZero
        Integer stock,

        @Schema(description = "ID de uma categoria existente")
        @NotNull
        UUID categoryId
) {
}