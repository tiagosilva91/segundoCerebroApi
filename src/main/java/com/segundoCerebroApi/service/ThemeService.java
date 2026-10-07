package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.ThemeRequestDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.exception.InvalidThemeException;
import com.segundoCerebroApi.exception.ResourceNotFoundException;
import com.segundoCerebroApi.exception.UnauthorizedAccessException;
import com.segundoCerebroApi.repository.ThemeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository repository;
    private final PlanService planService;

    @Transactional
    public ThemeResponseDTO create(ThemeRequestDTO dto, User user) {
        planService.assertCanCreateTheme(user);
        String name = dto.name().trim();
        if (repository.existsByUserAndNameIgnoreCase(user, name)) {
            throw new InvalidThemeException("Já existe um tema com o nome \"" + name + "\".");
        }
        Theme theme = repository.save(new Theme(null, name, user));
        return toDTO(theme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponseDTO> findAll(User user) {
        return repository.findAllByUser(user).stream()
                .sorted(Comparator.comparing(Theme::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ThemeResponseDTO findById(UUID id, User user) {
        return toDTO(findOwned(id, user));
    }

    @Transactional
    public ThemeResponseDTO update(UUID id, ThemeRequestDTO dto, User user) {
        Theme theme = findOwned(id, user);
        String name = dto.name().trim();
        if (repository.existsByUserAndNameIgnoreCaseAndIdNot(user, name, id)) {
            throw new InvalidThemeException("Já existe um tema com o nome \"" + name + "\".");
        }
        theme.setName(name);
        return toDTO(repository.save(theme));
    }

    @Transactional
    public void delete(UUID id, User user) {
        // As associações em note_themes são removidas via ON DELETE CASCADE; as notas permanecem.
        repository.delete(findOwned(id, user));
    }

    private Theme findOwned(UUID id, User user) {
        Theme theme = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema não encontrado"));
        if (!theme.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Acesso negado: este tema pertence a outro usuário.");
        }
        return theme;
    }

    private ThemeResponseDTO toDTO(Theme t) {
        return new ThemeResponseDTO(t.getId(), t.getName());
    }
}
