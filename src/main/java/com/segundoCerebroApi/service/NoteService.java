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
import com.segundoCerebroApi.service.bible.BibleReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
        // saveAndFlush, nao save: @CreationTimestamp/@UpdateTimestamp so sao
        // preenchidos no flush, que por padrao acontece no commit -- depois de
        // mapToResponse. Por isso o POST devolvia createdAt/updatedAt nulos
        // enquanto o GET seguinte trazia as datas corretas.
        note = noteRepository.saveAndFlush(note);

        String planMessage = planService.lastItemWarning(
                user, count, planService.noteLimit(user), "Esta foi a sua última nota");
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
        // Mesmo motivo do create: sem o flush, updatedAt volta com o valor antigo.
        return mapToResponse(noteRepository.saveAndFlush(note), null);
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
        note.setTitle(desescapar(dto.title()));
        note.setContent(desescapar(dto.content()));
        note.setAudioUrl(dto.audioUrl());
        note.setImageUrl(dto.imageUrl());
        note.setBiblicalReferences(normalizarReferencias(dto.biblicalReferences()));
        note.setThemes(resolveThemes(dto.themeIds(), user));
    }

    /**
     * Converte entidades HTML que chegam coladas de páginas web para o caractere
     * correspondente. Notas coladas do navegador vinham com {@code &gt;} no lugar
     * de {@code >}, quebrando citações em markdown logo na primeira linha.
     *
     * <p>A aplicação nunca armazena HTML — o conteúdo é tratado como texto —, então
     * não há perda: o que o usuário digita literalmente como "&amp;gt;" é raro o
     * bastante para não justificar manter o artefato de colagem em todas as notas.</p>
     */
    private static String desescapar(String texto) {
        if (texto == null || texto.indexOf('&') < 0) return texto;
        return texto
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                // &amp; por último: senão "&amp;lt;" viraria "<" em vez de "&lt;"
                .replace("&amp;", "&");
    }

    /**
     * Confere cada referência contra o catálogo e grava na forma canônica.
     * Duplicatas ("jo 3.16" e "João 3:16" na mesma nota) colapsam numa só.
     */
    private static List<String> normalizarReferencias(List<String> referencias) {
        if (referencias == null || referencias.isEmpty()) return new ArrayList<>();

        LinkedHashSet<String> unicas = new LinkedHashSet<>();
        for (String referencia : referencias) {
            unicas.add(BibleReferences.normalizar(referencia));
        }
        return new ArrayList<>(unicas);
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
