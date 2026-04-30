package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.NoteRequestDTO;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.service.NoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "Notas/Sermões")
public class NoteController {
    private final NoteService service;

    public NoteController(NoteService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<NoteResponseDTO> create(@RequestBody @Valid NoteRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, user));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findAll(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> getById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid NoteRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.update(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
