package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.Note;
import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.NoteRequestDTO;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.exception.InvalidThemeException;
import com.segundoCerebroApi.exception.ResourceNotFoundException;
import com.segundoCerebroApi.exception.UnauthorizedAccessException;
import com.segundoCerebroApi.repository.NoteRepository;
import com.segundoCerebroApi.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final ThemeRepository themeRepository;
    private final PlanService planService;

    @Transactional
    public NoteResponseDTO create(NoteRequestDTO dto, User user) {
        long count = planService.assertCanCreateNote(user);

        Note note = new Note();
        apply(note, dto, user);
        note.setUser(user);
        note = noteRepository.save(note);

        String planMessage = planService.lastItemWarning(user, count, planService.noteLimit(user), "nota");
        return mapToResponse(note, planMessage);
    }

    @Transactional(readOnly = true)
    public List<NoteResponseDTO> findAll(User user) {
        return noteRepository.findAllByUserOrderByUpdatedAtDesc(user).stream()
                .map(note -> mapToResponse(note, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponseDTO findById(UUID id, User user) {
        return mapToResponse(findOwned(id, user), null);
    }

    @Transactional
    public NoteResponseDTO update(UUID id, NoteRequestDTO dto, User user) {
        Note note = findOwned(id, user);
        apply(note, dto, user);
        return mapToResponse(noteRepository.save(note), null);
    }

    @Transactional
    public void delete(UUID id, User user) {
        noteRepository.delete(findOwned(id, user));
    }

    @Transactional(readOnly = true)
    public List<NoteResponseDTO> search(String query, User user) {
        if (query == null || query.isBlank()) return List.of();
        return noteRepository.search(user, query.trim()).stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(note -> mapToResponse(note, null))
                .toList();
    }

    // ─── helpers ──────────────────────────────────────────────────────

    private Note findOwned(UUID id, User user) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota não encontrada"));
        if (!note.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Acesso negado: esta nota pertence a outro usuário.");
        }
        return note;
    }

    /** Copia os campos do DTO para a entidade, validando que os temas pertencem ao usuário. */
    private void apply(Note note, NoteRequestDTO dto, User user) {
        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setAudioUrl(dto.audioUrl());
        note.setImageUrl(dto.imageUrl());
        note.setBiblicalReferences(dto.biblicalReferences() == null
                ? new ArrayList<>() : new ArrayList<>(dto.biblicalReferences()));
        note.setThemes(resolveThemes(dto.themeIds(), user));
    }

    private Set<Theme> resolveThemes(List<UUID> themeIds, User user) {
        Set<Theme> themes = new HashSet<>();
        if (themeIds != null && !themeIds.isEmpty()) {
            List<Theme> foundThemes = themeRepository.findAllByIdInAndUser(themeIds, user);
            if (foundThemes.size() != new HashSet<>(themeIds).size()) {
                throw new InvalidThemeException("Um ou mais temas informados são inválidos ou inacessíveis.");
            }
            themes.addAll(foundThemes);
        }
        return themes;
    }

    private NoteResponseDTO mapToResponse(Note note, String planMessage) {
        return new NoteResponseDTO(
                note.getId(), note.getTitle(), note.getContent(),
                note.getAudioUrl(), note.getImageUrl(), note.getBiblicalReferences(),
                note.getThemes().stream().map(t -> new ThemeResponseDTO(t.getId(), t.getName())).toList(),
                planMessage, note.getCreatedAt(), note.getUpdatedAt());
    }
}
