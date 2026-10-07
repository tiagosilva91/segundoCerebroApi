package com.segundoCerebroApi.dto.bible;

import java.util.List;

/** DTOs públicos do módulo da Bíblia. */
public final class BibleDTOs {

    private BibleDTOs() {}

    public record TranslationDTO(String code, String name) {}

    public record BookDTO(int id, String name, String abbrev, String testament, int chapters) {}

    public record VerseDTO(int verse, String text) {}

    public record PassageDTO(
            int bookId,
            String book,
            int chapter,
            Integer verseStart,
            Integer verseEnd,
            String translation,
            String reference,
            List<VerseDTO> verses
    ) {}

    public record SearchResultDTO(
            String reference,
            int bookId,
            String book,
            int chapter,
            int verse,
            String text,
            String translation
    ) {}
}
