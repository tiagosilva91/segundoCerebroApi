package com.segundoCerebroApi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.segundoCerebroApi.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Respostas de erro de autenticação e autorização no mesmo formato
 * {@link ErrorResponse} usado pelo resto da API.
 *
 * <p>Sem isso, o Spring devolvia 403 tanto para "não autenticado" quanto para
 * "autenticado mas sem permissão", no corpo padrão do Tomcat. O cliente não
 * conseguia distinguir "sua sessão expirou, faça login" de "você não tem acesso
 * a este recurso", e portanto não tinha como disparar logout automático.</p>
 */
@Component
public class RestAuthenticationHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAuthenticationHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Sem credenciais ou com token inválido/expirado. */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED,
                "Sessão inválida ou expirada. Faça login novamente.");
    }

    /** Autenticado, mas sem permissão para o recurso. */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(response, HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso.");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(status.value(), message, LocalDateTime.now()));
    }
}
