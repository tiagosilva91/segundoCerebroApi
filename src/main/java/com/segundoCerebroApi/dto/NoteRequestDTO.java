package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record NoteRequestDTO(
        @NotBlank String title,
        @NotBlank String content,
        String audioUrl,
        String imageUrl,
        @NotEmpty List<String> biblicalReferences,
        List<UUID> themeIds
) {}
