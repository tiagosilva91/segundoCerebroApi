package com.segundoCerebroApi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NoteResponseDTO(
        UUID id,
        String title,
        String content,
        String audioUrl,
        String imageUrl,
        List<String> biblicalReferences,
        List<ThemeResponseDTO> themes,
        String planMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
