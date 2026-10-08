package com.segundoCerebroApi.integration;

import com.segundoCerebroApi.domain.PlanType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre os dois caminhos de upgrade gratuito para PRO fechados no Sprint 0.
 *
 * <p>O perfil de teste usa {@code allow-simulated-upgrade=false}, que é o default
 * de produção, e um segredo de webhook conhecido.</p>
 */
class PagamentoIT extends IntegrationTestBase {

    private static final String EMAIL = "pastor@exemplo.com";
    private static final String SENHA = "SenhaSegura123";
    private static final String SEGREDO = "segredo-webhook-teste";

    private String criarUsuarioELogar() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Pastor Teste",
                                "email", EMAIL,
                                "birthDate", "1990-05-10",
                                "password", SENHA,
                                "cpf", "39053344705"))))
                .andExpect(status().isCreated());

        String corpo = mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", EMAIL, "password", SENHA))))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    private PlanType planoAtual() {
        return userRepository.findByEmail(EMAIL).orElseThrow().getPlanType();
    }

    @Test
    @DisplayName("webhook sem assinatura e rejeitado e nao concede PRO")
    void webhookSemAssinatura() throws Exception {
        criarUsuarioELogar();

        mockMvc.perform(post("/api/v1/payment/webhook").contentType(APPLICATION_JSON)
                        .content(json(Map.of("userEmail", EMAIL, "status", "paid"))))
                .andExpect(status().isForbidden());

        assertThat(planoAtual()).isEqualTo(PlanType.FREE);
    }

    @Test
    @DisplayName("webhook com assinatura errada e rejeitado e nao concede PRO")
    void webhookAssinaturaErrada() throws Exception {
        criarUsuarioELogar();

        mockMvc.perform(post("/api/v1/payment/webhook")
                        .header("X-Webhook-Secret", "errado")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("userEmail", EMAIL, "status", "paid"))))
                .andExpect(status().isForbidden());

        assertThat(planoAtual()).isEqualTo(PlanType.FREE);
    }

    @Test
    @DisplayName("webhook com a assinatura correta concede PRO")
    void webhookAssinaturaCorreta() throws Exception {
        criarUsuarioELogar();

        mockMvc.perform(post("/api/v1/payment/webhook")
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("userEmail", EMAIL, "status", "paid"))))
                .andExpect(status().isOk());

        assertThat(planoAtual()).isEqualTo(PlanType.PRO);
    }

    @Test
    @DisplayName("simulate-upgrade e bloqueado quando desabilitado (default de producao)")
    void simulateUpgradeBloqueado() throws Exception {
        String token = criarUsuarioELogar();

        mockMvc.perform(post("/api/v1/payment/simulate-upgrade")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(planoAtual()).isEqualTo(PlanType.FREE);
    }

    @Test
    @DisplayName("simulate-upgrade exige autenticacao")
    void simulateUpgradeExigeLogin() throws Exception {
        mockMvc.perform(post("/api/v1/payment/simulate-upgrade"))
                .andExpect(status().isUnauthorized());
    }
}
