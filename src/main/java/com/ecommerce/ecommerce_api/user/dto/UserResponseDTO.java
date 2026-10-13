package com.ecommerce.ecommerce_api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        @Schema(example = "Eduarda") String name,
        @Schema(example = "eduarda@example.com") String email,
        Instant createdAt
) {}