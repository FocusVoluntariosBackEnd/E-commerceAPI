package com.ecommerce.ecommerce_api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Token JWT para o header Authorization")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType
) {
}