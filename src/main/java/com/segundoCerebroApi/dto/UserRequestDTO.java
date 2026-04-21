package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRequestDTO(
        @NotBlank String name,
        @Email String email,
        @NotNull LocalDate birthDate,
        @NotBlank String password,
        @NotBlank @Pattern(regexp = "\\d{11}") String cpf
) {}
