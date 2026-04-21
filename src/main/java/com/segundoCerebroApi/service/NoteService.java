package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.Note;
import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.NoteRequestDTO;
import com.segundoCerebroApi.dto.NoteResponseDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.repository.NoteRepository;
import com.segundoCerebroApi.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public NoteResponseDTO create(NoteRequestDTO dto, User user) {
        // Busca os temas e garante que pertencem ao usuário
        Set<Theme> themes = new HashSet<>(themeRepository.findAllById(dto.themeIds()));

        Note note = new Note();
        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setAudioUrl(dto.audioUrl());
        note.setImageUrl(dto.imageUrl());
        note.setBiblicalReferences(dto.biblicalReferences());
        note.setUser(user);
        note.setThemes(themes);

        note = noteRepository.save(note);
        return mapToResponse(note);
    }

    public List<NoteResponseDTO> findAll(User user) {
        return noteRepository.findAllByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NoteResponseDTO mapToResponse(Note note) {
        return new NoteResponseDTO(
                note.getId(), note.getTitle(), note.getContent(),
                note.getAudioUrl(), note.getImageUrl(), note.getBiblicalReferences(),
                note.getThemes().stream().map(t -> new ThemeResponseDTO(t.getId(), t.getName())).toList()
        );
    }
}
