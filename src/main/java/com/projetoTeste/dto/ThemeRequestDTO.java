package com.projetoTeste.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequestDTO(@NotBlank String name) {}
