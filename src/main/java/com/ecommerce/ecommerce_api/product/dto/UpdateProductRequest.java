package com.ecommerce.ecommerce_api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
        @Schema(example = "Notebook")
        @NotBlank
        String name,

        @Schema(example = "Notebook para estudos")
        String description,

        @Schema(example = "3500.00")
        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @Schema(example = "10")
        @NotNull
        @PositiveOrZero
        Integer stock,

        @NotNull
        UUID categoryId,

        @Schema(example = "0", description = "Versão atual do produto (veja no GET). Se estiver desatualizada, retorna 409.")
        @NotNull
        Long version
) {
}