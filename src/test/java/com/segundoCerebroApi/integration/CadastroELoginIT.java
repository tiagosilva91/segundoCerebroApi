package com.segundoCerebroApi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre os defeitos de cadastro e sessão encontrados no QA de 2026-10-08.
 */
class CadastroELoginIT extends IntegrationTestBase {

    private static final String EMAIL = "pastor@exemplo.com";
    private static final String SENHA = "SenhaSegura123";
    private static final String CPF = "39053344705";

    private Map<String, Object> novoUsuario(String email, String cpf) {
        return Map.of(
                "name", "Pastor Teste",
                "email", email,
                "birthDate", "1990-05-10",
                "password", SENHA,
                "cpf", cpf);
    }

    private String cadastrarELogar() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(novoUsuario(EMAIL, CPF))))
                .andExpect(status().isCreated());

        String corpo = mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", EMAIL, "password", SENHA))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    @Test
    @DisplayName("cadastro valido devolve 201")
    void cadastroValido() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(novoUsuario(EMAIL, CPF))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.planType").value("FREE"));
    }

    @Test
    @DisplayName("e-mail duplicado devolve 409, nao 500")
    void emailDuplicado() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                .content(json(novoUsuario(EMAIL, CPF))));

        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(novoUsuario(EMAIL, "11122233396"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este e-mail já está cadastrado."));
    }

    @Test
    @DisplayName("CPF duplicado devolve 409, nao 500")
    void cpfDuplicado() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                .content(json(novoUsuario(EMAIL, CPF))));

        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(novoUsuario("outro@exemplo.com", CPF))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este CPF já está cadastrado."));
    }

    @Test
    @DisplayName("a senha nunca e gravada em texto puro")
    void senhaEhHasheada() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                .content(json(novoUsuario(EMAIL, CPF))));

        String armazenada = userRepository.findByEmail(EMAIL).orElseThrow().getPassword();
        assertThat(armazenada).isNotEqualTo(SENHA);
        assertThat(armazenada).startsWith("$2");
    }

    @Test
    @DisplayName("sem token devolve 401, nao 403")
    void semTokenEh401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("token invalido devolve 401")
    void tokenInvalidoEh401() throws Exception {
        mockMvc.perform(get("/api/v1/notes").header("Authorization", "Bearer lixo.invalido.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("usuario comum em rota de ADMIN devolve 403, nao 401")
    void usuarioComumEmRotaAdminEh403() throws Exception {
        String token = cadastrarELogar();

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("senha errada devolve 401 de negocio")
    void senhaErrada() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                .content(json(novoUsuario(EMAIL, CPF))));

        mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", EMAIL, "password", "errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("validacao de e-mail e CPF devolve 400 com o campo")
    void validacaoDeEntrada() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "X",
                                "email", "nao-eh-email",
                                "birthDate", "1990-01-01",
                                "password", "a",
                                "cpf", "123"))))
                .andExpect(status().isBadRequest());
    }
}
