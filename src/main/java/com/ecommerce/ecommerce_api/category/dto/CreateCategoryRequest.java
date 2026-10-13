package com.ecommerce.ecommerce_api.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank
        String name
) {
        public CreateCategoryRequest {
                if (name != null)
                        name = name.trim().toUpperCase();
        }
}
