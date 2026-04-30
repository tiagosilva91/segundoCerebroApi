package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;

public record FirstAccessPasswordDTO(@NotBlank String password) {}
