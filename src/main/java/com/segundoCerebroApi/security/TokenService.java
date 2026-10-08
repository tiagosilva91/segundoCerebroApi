package com.segundoCerebroApi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.segundoCerebroApi.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    /** Validade do token de acesso. Exposta para o cliente alinhar o cookie. */
    public static final Duration TOKEN_VALIDITY = Duration.ofHours(2);

    @Value("${api.security.token.secret}")
    private String secret;

    /**
     * Gera o token JWT para o usuário autenticado.
     * O "subject" do token será o email do usuário.
     */
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    /**
     * Valida o token recebido e retorna o email (subject) se estiver ok.
     * Caso o token seja inválido ou expirado, retorna uma String vazia.
     */
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    /**
     * Define o tempo de expiração do token.
     *
     * <p>A versão anterior fazia {@code LocalDateTime.now().plusMinutes(30)
     * .toInstant(ZoneOffset.of("-03:00"))}: lia o relógio no fuso do servidor e
     * depois reinterpretava aquele horário como -03:00, somando 3 horas
     * artificiais. No container (UTC) o token durava ~3h30; numa máquina em -03,
     * 30 minutos. A validade mudava conforme o ambiente.</p>
     *
     * <p>{@link Instant} já é um ponto absoluto na linha do tempo, então não há
     * fuso envolvido.</p>
     */
    private Instant genExpirationDate() {
        return Instant.now().plus(TOKEN_VALIDITY);
    }
}