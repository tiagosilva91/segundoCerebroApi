package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.ThemeRequestDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.service.ThemeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/themes")
@Tag(name = "Temas")
public class ThemeController {
    private final ThemeService service;

    public ThemeController(ThemeService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ThemeResponseDTO> create(@RequestBody @Valid ThemeRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, user));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponseDTO>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findAll(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponseDTO> getById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThemeResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid ThemeRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.update(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}