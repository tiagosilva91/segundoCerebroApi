package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.*;
import com.segundoCerebroApi.exception.InvalidCredentialsException;
import com.segundoCerebroApi.repository.UserRepository;
import com.segundoCerebroApi.security.TokenService;
import com.segundoCerebroApi.service.PasswordResetService;
import com.segundoCerebroApi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          TokenService tokenService,
                          PasswordEncoder passwordEncoder,
                          PasswordResetService passwordResetService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza o login", description = "Retorna um token JWT válido para o usuário")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data) {
        var user = userRepository.findByEmail(data.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new InvalidCredentialsException("E-mail ou senha inválidos.");
        }

        var token = tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(
                token,
                Boolean.TRUE.equals(user.getFirstLogin()),
                user.getPlanType()
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Busca dados da sessão", description = "Retorna os dados do usuário logado baseado no Token JWT")
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    @PostMapping("/first-access-password")
    @Operation(summary = "Altera a senha no primeiro acesso", description = "Altera a senha e finaliza o primeiro login")
    public ResponseEntity<Void> updateFirstAccessPassword(@AuthenticationPrincipal User user,
                                                          @RequestBody @Valid FirstAccessPasswordDTO data) {
        userService.updatePasswordFirstAccess(user.getId(), data.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Altera senha do usuário autenticado")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               @RequestBody @Valid ChangePasswordDTO data) {
        if (!passwordEncoder.matches(data.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }
        userService.updatePasswordFirstAccess(user.getId(), data.newPassword());
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
    public ResponseEntity<String> resetPassword(
            @RequestParam(required = false) String token,
            @RequestBody @Valid ResetPasswordRequestDTO request) {

        String tokenToUse = (request.getToken() != null && !request.getToken().isBlank())
                ? request.getToken()
                : token;

        if (tokenToUse == null || tokenToUse.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token não informado.");
        }

        boolean success = passwordResetService.resetPassword(tokenToUse, request.getNewPassword());
        if (success) {
            return ResponseEntity.ok("Senha redefinida com sucesso.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
    }
}