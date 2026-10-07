package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.dto.bible.BibleDTOs.*;
import com.segundoCerebroApi.service.bible.BibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bible")
@Tag(name = "Bíblia", description = "Livros, versões e textos bíblicos")
public class BibleController {

    private static final CacheControl LONG_CACHE = CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final BibleService service;

    public BibleController(BibleService service) {
        this.service = service;
    }

    @GetMapping("/translations")
    @Operation(summary = "Lista as versões disponíveis")
    public ResponseEntity<List<TranslationDTO>> translations() {
        return ResponseEntity.ok().cacheControl(LONG_CACHE).body(service.listTranslations());
    }

    @GetMapping("/books")
    @Operation(summary = "Lista os 66 livros", description = "Filtro opcional por testamento: AT ou NT")
    public ResponseEntity<List<BookDTO>> books(@RequestParam(required = false) String testament) {
        return ResponseEntity.ok().cacheControl(LONG_CACHE).body(service.listBooks(testament));
    }

    @GetMapping("/books/{book}")
    @Operation(summary = "Detalhes de um livro", description = "Aceita id (1-66), nome (João) ou abreviação (Jo)")
    public ResponseEntity<BookDTO> book(@PathVariable String book) {
        return ResponseEntity.ok().cacheControl(LONG_CACHE).body(service.getBook(book));
    }

    @GetMapping("/passages")
    @Operation(summary = "Busca um capítulo ou trecho",
            description = "Sem verseStart retorna o capítulo inteiro. Ex.: ?book=João&chapter=3&translation=ARA&verseStart=16&verseEnd=18")
    public ResponseEntity<PassageDTO> passage(
            @Parameter(description = "id, nome ou abreviação") @RequestParam String book,
            @RequestParam int chapter,
            @RequestParam(required = false) String translation,
            @RequestParam(required = false) Integer verseStart,
            @RequestParam(required = false) Integer verseEnd) {
        return ResponseEntity.ok().cacheControl(LONG_CACHE)
                .body(service.getPassage(book, chapter, translation, verseStart, verseEnd));
    }

    @GetMapping("/reference")
    @Operation(summary = "Busca por referência textual", description = "Ex.: ?ref=Romanos 8:28-30&translation=NAA")
    public ResponseEntity<PassageDTO> byReference(@RequestParam String ref,
                                                  @RequestParam(required = false) String translation) {
        return ResponseEntity.ok().cacheControl(LONG_CACHE).body(service.getByReference(ref, translation));
    }

    @GetMapping("/search")
    @Operation(summary = "Pesquisa por palavras no texto bíblico")
    public ResponseEntity<List<SearchResultDTO>> search(@RequestParam String q,
                                                        @RequestParam(required = false) String translation,
                                                        @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.search(q, translation, limit));
    }
}
