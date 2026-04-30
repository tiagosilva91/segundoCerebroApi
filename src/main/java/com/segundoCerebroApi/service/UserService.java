package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.UserRequestDTO;
import com.segundoCerebroApi.dto.UserResponseDTO;
import com.segundoCerebroApi.exception.UserNotFoundException;
import com.segundoCerebroApi.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);

        String hash = passwordEncoder.encode(dto.password());
        user.setPassword(hash);

        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado na base de dados"));
    }

    @Transactional
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado na base de dados"));

        BeanUtils.copyProperties(dto, user, "id"); // "id" é ignorado para não sobrescrever
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Usuário com ID " + id + " não encontrado na base de dados");
        }
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o email: " + email));
    }

    @Transactional
    public void updatePasswordFirstAccess(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado na base de dados"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}