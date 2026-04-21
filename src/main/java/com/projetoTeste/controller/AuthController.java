package com.projetoTeste.controller;

import com.projetoTeste.domain.User;
import com.projetoTeste.dto.LoginRequestDTO;
import com.projetoTeste.dto.LoginResponseDTO;
import com.projetoTeste.dto.UserResponseDTO;
import com.projetoTeste.security.TokenService;
import com.projetoTeste.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para login e gestão de sessão")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza o login", description = "Retorna um token JWT válido para o usuário")
    public ResponseEntity login(@RequestBody LoginRequestDTO data) {
        var user = userService.findByEmail(data.email());

        if (passwordEncoder.matches(data.password(), user.getPassword())) {
            var token = tokenService.generateToken(user);
            return ResponseEntity.ok(new LoginResponseDTO(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    @Operation(summary = "Busca dados da sessão", description = "Retorna os dados do usuário logado baseado no Token JWT")
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal User user) {
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate()
        );
        return ResponseEntity.ok(response);
    }
}