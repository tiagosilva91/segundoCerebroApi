package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record NoteRequestDTO(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        @Size(max = 255) String audioUrl,
        @Size(max = 255) String imageUrl,
        List<@NotBlank @Size(max = 255) String> biblicalReferences, // opcional (pode ser vazio)
        List<UUID> themeIds
) {}
