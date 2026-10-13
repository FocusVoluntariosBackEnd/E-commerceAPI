package com.ecommerce.ecommerce_api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "eduarda@example.com")
        @NotBlank
        String email,

        @Schema(example = "SenhaForte123!")
        @NotBlank
        String password
) {
}