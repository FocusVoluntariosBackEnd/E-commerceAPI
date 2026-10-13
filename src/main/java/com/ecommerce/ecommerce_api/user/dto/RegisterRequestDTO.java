package com.ecommerce.ecommerce_api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @Schema(example = "Eduarda")
        @NotBlank(message = "o nome é Obrigatório")
        String name,

        @Schema(example = "eduarda@example.com")
        @NotBlank (message = "O email é Obrigatorio")
        @Email(message = "Formato de Email invalido")
        String email,

        @Schema(example = "SenhaForte123!", description = "Mínimo de 8 caracteres")
        @NotBlank(message = "A senha é Obrigatorio")
        @Size(min = 8, message = "A senha deve ter no Minimo 8 Caracteres")
        String password

) {}