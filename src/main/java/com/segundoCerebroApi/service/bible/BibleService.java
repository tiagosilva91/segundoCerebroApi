package com.segundoCerebroApi.service.bible;

import com.segundoCerebroApi.config.AppProperties;
import com.segundoCerebroApi.dto.bible.BibleDTOs.*;
import com.segundoCerebroApi.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BibleService {

    /** Ex.: "João 3", "João 3:16", "1 Co 13:4-7", "Sl 23.1-3" */
    private static final Pattern REFERENCE = Pattern.compile("^\\s*(.+?)\\s+(\\d+)(?:[:.](\\d+)(?:\\s*-\\s*(\\d+))?)?\\s*$");
    private static final Pattern FOOTNOTES = Pattern.compile("<sup>.*?</sup>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");

    private static final int MAX_SEARCH_RESULTS = 50;

    private final BollsBibleClient client;
    private final String defaultTranslation;

    public BibleService(BollsBibleClient client, AppProperties props) {
        this.client = client;
        this.defaultTranslation = props.bible().defaultTranslation();
    }

    public List<TranslationDTO> listTranslations() {
        return BibleTranslations.ALL.stream()
                .map(t -> new TranslationDTO(t.code(), t.name()))
                .toList();
    }

    public List<BookDTO> listBooks(String testament) {
        return BibleCatalog.BOOKS.stream()
                .filter(b -> testament == null || testament.isBlank() || b.testament().name().equalsIgnoreCase(testament))
                .map(this::toDTO)
                .toList();
    }

    public BookDTO getBook(String idOrName) {
        return toDTO(resolveBook(idOrName));
    }

    public PassageDTO getPassage(String book, int chapter, String translation, Integer verseStart, Integer verseEnd) {
        BibleCatalog.Book b = resolveBook(book);
        String tr = resolveTranslation(translation);

        if (chapter < 1 || chapter > b.chapters()) {
            throw new IllegalArgumentException(b.name() + " possui capítulos de 1 a " + b.chapters() + ".");
        }
        if (verseStart != null && verseStart < 1) {
            throw new IllegalArgumentException("verseStart deve ser maior que zero.");
        }
        if (verseEnd != null && verseStart == null) {
            throw new IllegalArgumentException("verseEnd requer verseStart.");
        }
        if (verseEnd != null && verseEnd < verseStart) {
            throw new IllegalArgumentException("verseEnd deve ser maior ou igual a verseStart.");
        }

        int end = verseEnd != null ? verseEnd : (verseStart != null ? verseStart : Integer.MAX_VALUE);
        int start = verseStart != null ? verseStart : 1;

        List<VerseDTO> verses = client.getChapter(tr, b.id(), chapter).stream()
                .filter(v -> v.verse() >= start && v.verse() <= end)
                .map(v -> new VerseDTO(v.verse(), clean(v.text())))
                .toList();

        if (verses.isEmpty()) {
            throw new ResourceNotFoundException("Passagem não encontrada: " + formatReference(b, chapter, verseStart, verseEnd));
        }

        return new PassageDTO(b.id(), b.name(), chapter, verseStart, verseEnd, tr,
                formatReference(b, chapter, verseStart, verseEnd), verses);
    }

    /** Resolve uma referência textual (ex.: "Romanos 8:28-30") e retorna a passagem. */
    public PassageDTO getByReference(String reference, String translation) {
        Matcher m = REFERENCE.matcher(reference == null ? "" : reference);
        if (!m.matches()) {
            throw new IllegalArgumentException("Referência inválida. Use o formato \"Livro capítulo[:versículo[-versículo]]\", ex.: João 3:16.");
        }
        Integer vs = m.group(3) != null ? Integer.valueOf(m.group(3)) : null;
        Integer ve = m.group(4) != null ? Integer.valueOf(m.group(4)) : null;
        return getPassage(m.group(1), Integer.parseInt(m.group(2)), translation, vs, ve);
    }

    public List<SearchResultDTO> search(String query, String translation, int limit) {
        if (query == null || query.trim().length() < 3) {
            throw new IllegalArgumentException("Digite ao menos 3 caracteres para pesquisar.");
        }
        String tr = resolveTranslation(translation);
        int safeLimit = Math.max(1, Math.min(limit, MAX_SEARCH_RESULTS));

        return client.search(tr, query.trim(), safeLimit).results().stream()
                .filter(v -> v.book() != null && v.chapter() != null)
                .map(v -> {
                    BibleCatalog.Book b = BibleCatalog.findById(v.book()).orElse(null);
                    if (b == null) return null;
                    return new SearchResultDTO(
                            b.name() + " " + v.chapter() + ":" + v.verse(),
                            b.id(), b.name(), v.chapter(), v.verse(), clean(v.text()), tr);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // ─── helpers ──────────────────────────────────────────────────────

    private BibleCatalog.Book resolveBook(String idOrName) {
        return BibleCatalog.find(idOrName)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado: " + idOrName));
    }

    private String resolveTranslation(String translation) {
        if (translation == null || translation.isBlank()) return defaultTranslation;
        return BibleTranslations.find(translation)
                .map(BibleTranslations.Translation::code)
                .orElseThrow(() -> new IllegalArgumentException("Versão não suportada: " + translation
                        + ". Consulte GET /api/v1/bible/translations."));
    }

    private BookDTO toDTO(BibleCatalog.Book b) {
        return new BookDTO(b.id(), b.name(), b.abbrev(), b.testament().name(), b.chapters());
    }

    private static String formatReference(BibleCatalog.Book b, int chapter, Integer vs, Integer ve) {
        StringBuilder sb = new StringBuilder(b.name()).append(' ').append(chapter);
        if (vs != null) {
            sb.append(':').append(vs);
            if (ve != null && !ve.equals(vs)) sb.append('-').append(ve);
        }
        return sb.toString();
    }

    /** Remove notas de rodapé, tags HTML e entidades do texto do provedor. */
    static String clean(String html) {
        if (html == null) return "";
        String noNotes = FOOTNOTES.matcher(html).replaceAll("");
        String noTags = TAGS.matcher(noNotes).replaceAll("");
        return HtmlUtils.htmlUnescape(noTags).replaceAll("\\s+", " ").trim();
    }
}
