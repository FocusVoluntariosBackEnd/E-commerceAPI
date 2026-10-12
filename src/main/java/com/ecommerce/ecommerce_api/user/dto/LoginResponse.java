package com.ecommerce.ecommerce_api.user.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
