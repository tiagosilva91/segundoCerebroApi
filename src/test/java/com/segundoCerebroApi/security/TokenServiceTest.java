package com.segundoCerebroApi.security;

import com.auth0.jwt.JWT;
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

    private TokenService novoService() {
        TokenService service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "segredo-de-teste");
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

        assertThat(service.validateToken(token)).isEqualTo("pastor@exemplo.com");
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
