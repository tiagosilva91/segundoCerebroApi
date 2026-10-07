package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.FirstAccessPasswordDTO;
import com.segundoCerebroApi.dto.LoginRequestDTO;
import com.segundoCerebroApi.dto.LoginResponseDTO;
import com.segundoCerebroApi.dto.UserResponseDTO;
import com.segundoCerebroApi.security.TokenService;
import com.segundoCerebroApi.service.UserService;
import com.segundoCerebroApi.service.PasswordResetService;
import com.segundoCerebroApi.dto.ForgotPasswordRequestDTO;
import com.segundoCerebroApi.dto.ResetPasswordRequestDTO;
import jakarta.validation.Valid;
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
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, TokenService tokenService, PasswordEncoder passwordEncoder, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza o login", description = "Retorna um token JWT válido para o usuário")
    public ResponseEntity login(@RequestBody LoginRequestDTO data) {
        var user = userService.findByEmail(data.email());

        if (passwordEncoder.matches(data.password(), user.getPassword())) {
            var token = tokenService.generateToken(user);
            return ResponseEntity.ok(new LoginResponseDTO(token, user.getFirstLogin()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    @Operation(summary = "Busca dados da sessão", description = "Retorna os dados do usuário logado baseado no Token JWT")
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    @PostMapping("/first-access-password")
    @Operation(summary = "Altera a senha no primeiro acesso", description = "Altera a senha e finaliza o primeiro login")
    public ResponseEntity<Void> updateFirstAccessPassword(@AuthenticationPrincipal User user, @RequestBody FirstAccessPasswordDTO data) {
        userService.updatePasswordFirstAccess(user.getId(), data.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicita recuperação de senha", description = "Gera um token de recuperação e envia por email")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefine a senha", description = "Utiliza o token recebido por email para criar uma nova senha")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok("Senha redefinida com sucesso.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
    }
}