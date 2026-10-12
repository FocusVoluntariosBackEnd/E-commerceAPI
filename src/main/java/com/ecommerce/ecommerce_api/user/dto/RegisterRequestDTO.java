package com.ecommerce.ecommerce_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "o nome é Obrigatório")
        String name,

        @NotBlank (message = "O email é Obrigatorio")
        @Email(message = "Formato de Email invalido")
        String email,

        @NotBlank(message = "A senha é Obrigatorio")
        @Size(min = 8, message = "A senha deve ter no Minimo 8 Caracteres")
        String password

) {}
