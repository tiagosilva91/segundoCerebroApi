package com.projetoTeste.service;

import com.projetoTeste.domain.Theme;
import com.projetoTeste.domain.User;
import com.projetoTeste.dto.ThemeRequestDTO;
import com.projetoTeste.dto.ThemeResponseDTO;
import com.projetoTeste.repository.ThemeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<ThemeResponseDTO> findAll(User user) {
        return repository.findAllByUser(user).stream()
                .map(t -> new ThemeResponseDTO(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }
}
