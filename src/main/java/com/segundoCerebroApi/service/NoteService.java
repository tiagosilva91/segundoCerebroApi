package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.Note;
import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.domain.PlanType;
import com.segundoCerebroApi.dto.NoteRequestDTO;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.exception.PlanLimitExceededException;
import com.segundoCerebroApi.repository.NoteRepository;
import com.segundoCerebroApi.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public NoteResponseDTO create(NoteRequestDTO dto, User user) {
        long count = noteRepository.countByUser(user);
        if (user.getPlanType() == PlanType.FREE && count >= 5) {
            throw new PlanLimitExceededException("Você atingiu o limite de 5 notas do plano FREE.");
        }

        // Busca os temas e garante que pertencem ao usuário (com proteção para lista nula/vazia)
        Set<Theme> themes = (dto.themeIds() != null && !dto.themeIds().isEmpty())
                ? new HashSet<>(themeRepository.findAllById(dto.themeIds()))
                : new HashSet<>();

        Note note = new Note();
        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setAudioUrl(dto.audioUrl());
        note.setImageUrl(dto.imageUrl());
        note.setBiblicalReferences(dto.biblicalReferences());
        note.setUser(user);
        note.setThemes(themes);

        note = noteRepository.save(note);

        String planMessage = null;
        if (user.getPlanType() == PlanType.FREE && count == 4) {
            planMessage = "Esta é a sua última nota do plano FREE. Deseja migrar para o plano PRO?";
        }

        return mapToResponse(note, planMessage);
    }

    @Transactional(readOnly = true)
    public List<NoteResponseDTO> findAll(User user) {
        return noteRepository.findAllByUser(user).stream()
                .map(note -> mapToResponse(note, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponseDTO findById(UUID id, User user) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        return mapToResponse(note, null);
    }

    @Transactional
    public NoteResponseDTO update(UUID id, NoteRequestDTO dto, User user) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        Set<Theme> themes = new HashSet<>(themeRepository.findAllById(dto.themeIds()));

        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setAudioUrl(dto.audioUrl());
        note.setImageUrl(dto.imageUrl());
        note.setBiblicalReferences(dto.biblicalReferences());
        note.setThemes(themes);

        return mapToResponse(noteRepository.save(note), null);
    }

    @Transactional
    public void delete(UUID id, User user) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        noteRepository.delete(note);
    }

    private NoteResponseDTO mapToResponse(Note note, String planMessage) {
        return new NoteResponseDTO(
                note.getId(), note.getTitle(), note.getContent(),
                note.getAudioUrl(), note.getImageUrl(), note.getBiblicalReferences(),
                note.getThemes().stream().map(t -> new ThemeResponseDTO(t.getId(), t.getName())).toList(),
                planMessage
        );
    }
}
