package com.segundoCerebroApi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.segundoCerebroApi.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de unidade da validade do token.
 *
 * <p>A implementacao anterior fazia {@code LocalDateTime.now().plusMinutes(30)
 * .toInstant(ZoneOffset.of("-03:00"))}, somando 3 horas artificiais quando o
 * servidor nao estava em -03:00. Este teste falharia com aquele codigo em
 * qualquer maquina fora daquele fuso.</p>
 */
class TokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste";

    private TokenService novoService() {
        TokenService service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", SEGREDO);
        return service;
    }

    private User usuario() {
        User user = new User();
        user.setEmail("pastor@exemplo.com");
        return user;
    }

    @Test
    @DisplayName("o token expira em 2 horas, independentemente do fuso do servidor")
    void expiraEmDuasHoras() {
        Instant antes = Instant.now();
        String token = novoService().generateToken(usuario());
        Instant expiracao = JWT.decode(token).getExpiresAtAsInstant();

        Duration validade = Duration.between(antes, expiracao);

        assertThat(validade).isBetween(
                TokenService.TOKEN_VALIDITY.minusSeconds(30),
                TokenService.TOKEN_VALIDITY.plusSeconds(30));
    }

    @Test
    @DisplayName("o token gerado e validado de volta para o mesmo e-mail")
    void validaDeVolta() {
        TokenService service = novoService();
        String token = service.generateToken(usuario());

        assertThat(service.validateToken(token)).contains("pastor@exemplo.com");
    }

    @Test
    @DisplayName("token expirado nao valida (nao pode virar subject vazio)")
    void tokenExpirado() {
        TokenService service = novoService();
        String expirado = JWT.create()
                .withIssuer("auth-api")
                .withSubject("pastor@exemplo.com")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SEGREDO));

        assertThat(service.validateToken(expirado)).isEmpty();
    }

    @Test
    @DisplayName("token sem subject nao valida")
    void tokenSemSubject() {
        String semSubject = JWT.create()
                .withIssuer("auth-api")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256(SEGREDO));

        assertThat(novoService().validateToken(semSubject)).isEmpty();
    }

    @Test
    @DisplayName("token adulterado nao valida")
    void tokenAdulterado() {
        assertThat(novoService().validateToken("lixo.invalido.token")).isEmpty();
    }

    @Test
    @DisplayName("token assinado com outro segredo nao valida")
    void tokenDeOutroSegredo() {
        TokenService outro = new TokenService();
        ReflectionTestUtils.setField(outro, "secret", "outro-segredo");
        String token = outro.generateToken(usuario());

        assertThat(novoService().validateToken(token)).isEmpty();
    }
}
