package com.ecommerce.ecommerce_api.product.dto;

import com.ecommerce.ecommerce_api.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        @Schema(example = "Notebook") String name,
        @Schema(example = "Notebook para estudos") String description,
        @Schema(example = "3500.00") BigDecimal price,
        @Schema(example = "10") int stock,
        @Schema(example = "ELETRÔNICOS") String categoryName,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getName(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}