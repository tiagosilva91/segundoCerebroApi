package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.NoteRequestDTO;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.service.NoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@Validated
@Tag(name = "Notas/Sermões")
public class NoteController {
    private final NoteService service;

    public NoteController(NoteService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<NoteResponseDTO> create(@RequestBody @Valid NoteRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, user));
    }

    /**
     * Lista as notas do usuário, da mais recente para a mais antiga.
     *
     * <p>A paginação é opcional para não quebrar o contrato existente: sem
     * {@code page}/{@code size} a resposta continua sendo o array completo, como
     * o front espera hoje. Informando {@code size}, devolve só aquela fatia —
     * caminho a usar conforme o acervo cresce, já que o plano PRO é ilimitado.
     * O total vai no header {@code X-Total-Count} nos dois casos.</p>
     */
    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(200) Integer size) {

        List<NoteResponseDTO> todas = service.findAll(user);

        if (size == null) {
            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(todas.size()))
                    .body(todas);
        }

        int pagina = page == null ? 0 : page;
        int inicio = Math.min(pagina * size, todas.size());
        int fim = Math.min(inicio + size, todas.size());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(todas.size()))
                .body(todas.subList(inicio, fim));
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
