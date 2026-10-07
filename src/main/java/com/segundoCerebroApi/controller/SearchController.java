package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.dto.bible.BibleDTOs.SearchResultDTO;
import com.segundoCerebroApi.exception.ExternalServiceException;
import com.segundoCerebroApi.service.NoteService;
import com.segundoCerebroApi.service.bible.BibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Busca", description = "Busca generalizada em notas e na Bíblia")
public class SearchController {

    public record SearchResultsDTO(List<NoteResponseDTO> notes, List<SearchResultDTO> biblePassages) {}

    private final NoteService noteService;
    private final BibleService bibleService;

    public SearchController(NoteService noteService, BibleService bibleService) {
        this.noteService = noteService;
        this.bibleService = bibleService;
    }

    @GetMapping
    @Operation(summary = "Busca em notas (título, conteúdo, temas, referências) e no texto bíblico")
    public ResponseEntity<SearchResultsDTO> search(@RequestParam String q,
                                                   @RequestParam(required = false) String translation,
                                                   @AuthenticationPrincipal User user) {
        List<NoteResponseDTO> notes = noteService.search(q, user);

        List<SearchResultDTO> passages = List.of();
        if (q != null && q.trim().length() >= 3) {
            try {
                passages = bibleService.search(q, translation, 20);
            } catch (ExternalServiceException | IllegalArgumentException e) {
                // A busca na Bíblia é complementar: se o provedor falhar, retornamos apenas as notas.
            }
        }
        return ResponseEntity.ok(new SearchResultsDTO(notes, passages));
    }
}
