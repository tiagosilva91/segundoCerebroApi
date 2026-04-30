package com.segundoCerebroApi.dto;

import com.segundoCerebroApi.domain.User;

import com.segundoCerebroApi.domain.UserRole;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(UUID id, String name, String email, LocalDate birthDate, UserRole role) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getBirthDate(), user.getRole());
    }
}