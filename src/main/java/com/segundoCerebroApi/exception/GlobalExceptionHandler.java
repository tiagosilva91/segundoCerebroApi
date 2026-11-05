package com.segundoCerebroApi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message, LocalDateTime.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlePlanLimitExceeded(PlanLimitExceededException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Rede de segurança para violações de unicidade que escapem da checagem prévia
     * (ex.: dois cadastros simultâneos com o mesmo e-mail). Antes disso, a exceção
     * subia até o Tomcat e o cliente recebia 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        log.warn("Violação de integridade: {}", cause);

        String lower = cause == null ? "" : cause.toLowerCase();

        // Nem toda violação é duplicidade. A versão anterior decidia a mensagem
        // só por "contém o nome da coluna", então um NOT NULL ou um valor longo
        // demais viravam 409 "já cadastrado" — e o front mandava o usuário para
        // um login impossível. Agora o tipo da violação vem antes do campo.
        if (lower.contains("cannot be null") || lower.contains("not-null")) {
            return build(HttpStatus.BAD_REQUEST, "Campo obrigatório não informado: " + campo(lower) + ".");
        }
        if (lower.contains("data too long") || lower.contains("value too long")) {
            return build(HttpStatus.BAD_REQUEST, "Conteúdo maior que o limite permitido para " + campo(lower) + ".");
        }

        if (lower.contains("duplicate") || lower.contains("unique")) {
            if (lower.contains("email")) return build(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
            if (lower.contains("cpf")) return build(HttpStatus.CONFLICT, "Este CPF já está cadastrado.");
            return build(HttpStatus.CONFLICT, "Registro já existente.");
        }

        return build(HttpStatus.BAD_REQUEST, "Não foi possível salvar: dados inválidos.");
    }

    /** Extrai o campo citado pelo banco, para a mensagem apontar o que corrigir. */
    private static String campo(String causaEmMinusculas) {
        if (causaEmMinusculas.contains("email")) return "e-mail";
        if (causaEmMinusculas.contains("cpf")) return "CPF";
        if (causaEmMinusculas.contains("content")) return "conteúdo";
        if (causaEmMinusculas.contains("title")) return "título";
        if (causaEmMinusculas.contains("name")) return "nome";
        return "um dos campos";
    }

    @ExceptionHandler(InvalidThemeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTheme(InvalidThemeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Requisição inválida" : message);
    }

    /**
     * Violações em @RequestParam/@PathVariable de controllers @Validated. Sem
     * este handler, um {@code ?size=0} subiria como erro não tratado e viraria
     * 500 no catch-all.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String campo = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return campo + ": " + v.getMessage();
                })
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Requisição inválida" : message);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadParams(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido ou ausente: " + ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    /**
     * Repassa as exceções do Spring Security para o {@code ExceptionTranslationFilter},
     * que as converte em 401/403 via {@code RestAuthenticationHandlers}. Sem este
     * método, o catch-all abaixo as capturaria e devolveria 500 no lugar.
     */
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public void handleSpringSecurity(RuntimeException ex) {
        throw ex;
    }

    /**
     * Rede final: qualquer falha não prevista vira 500 no formato ErrorResponse,
     * sem expor a mensagem interna. Antes, essas exceções subiam até o Tomcat e o
     * cliente recebia o corpo padrão do Spring, com o path da requisição.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erro não tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente em instantes.");
    }
}