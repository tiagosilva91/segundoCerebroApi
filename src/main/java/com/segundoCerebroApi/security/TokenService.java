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
import java.util.Optional;

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
                    // O cliente já lia estas claims (api.ts), mas nenhuma era
                    // emitida: sem "id" ele caía no subject — o e-mail — e
                    // montava URLs de usuário com ele. São informativas; a
                    // autorização continua vindo do banco a cada requisição.
                    .withClaim("id", user.getId() == null ? null : user.getId().toString())
                    .withClaim("role", user.getRole() == null ? null : user.getRole().name())
                    .withClaim("planType", user.getPlanType() == null ? null : user.getPlanType().name())
                    .withClaim("firstLogin", Boolean.TRUE.equals(user.getFirstLogin()))
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    /**
     * Valida o token recebido e devolve o email (subject) quando ele é legítimo.
     *
     * <p>A versão anterior devolvia {@code ""} em caso de falha. Como o
     * {@code SecurityFilter} usava esse retorno direto num
     * {@code findByEmail(...)}, qualquer token inválido, forjado ou expirado
     * passava a ser resolvido como o usuário de e-mail vazio — e bastava
     * alguém cadastrar uma conta com {@code email: ""} (o DTO aceitava, ver
     * {@code UserRequestDTO}) para que <em>qualquer</em> string no header
     * Authorization abrisse sessão. Devolver {@link Optional#empty()} torna a
     * falha impossível de confundir com um subject válido.</p>
     */
    public Optional<String> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

            // Um token sem subject (ou com subject em branco) é tão inútil
            // quanto um token inválido: não identifica ninguém.
            if (subject == null || subject.isBlank()) return Optional.empty();
            return Optional.of(subject);
        } catch (JWTVerificationException exception) {
            return Optional.empty();
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