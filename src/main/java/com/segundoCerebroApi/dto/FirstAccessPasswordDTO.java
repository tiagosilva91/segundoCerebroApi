package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FirstAccessPasswordDTO(
        @NotBlank @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres") String password
) {}
