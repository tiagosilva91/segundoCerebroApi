package com.projetoTeste.dto;

import com.projetoTeste.domain.User;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(UUID id, String name, String email, LocalDate birthDate) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getBirthDate());
    }
}