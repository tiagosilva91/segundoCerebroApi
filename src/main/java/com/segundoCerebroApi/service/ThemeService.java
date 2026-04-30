package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.ThemeRequestDTO;
import com.segundoCerebroApi.dto.ThemeResponseDTO;
import com.segundoCerebroApi.repository.ThemeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository repository;

    @Transactional
    public ThemeResponseDTO create(ThemeRequestDTO dto, User user) {
        Theme theme = new Theme(null, dto.name(), user);
        theme = repository.save(theme);
        return new ThemeResponseDTO(theme.getId(), theme.getName());
    }

    @Transactional(readOnly = true)
    public List<ThemeResponseDTO> findAll(User user) {
        return repository.findAllByUser(user).stream()
                .map(t -> new ThemeResponseDTO(t.getId(), t.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ThemeResponseDTO findById(UUID id, User user) {
        Theme theme = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado"));
        if (!theme.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        return new ThemeResponseDTO(theme.getId(), theme.getName());
    }

    @Transactional
    public void delete(UUID id, User user) {
        Theme theme = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado"));
        if (!theme.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        repository.delete(theme);
    }
}
