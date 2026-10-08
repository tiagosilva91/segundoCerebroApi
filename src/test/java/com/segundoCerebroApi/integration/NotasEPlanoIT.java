package com.segundoCerebroApi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre isolamento entre usuários, limites de plano e os contratos de resposta
 * que o frontend depende (204 no delete, timestamps no POST).
 */
class NotasEPlanoIT extends IntegrationTestBase {

    private static final String SENHA = "SenhaSegura123";

    private String criarUsuario(String email, String cpf) throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Pastor " + email,
                                "email", email,
                                "birthDate", "1990-05-10",
                                "password", SENHA,
                                "cpf", cpf))))
                .andExpect(status().isCreated());

        String corpo = mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", SENHA))))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    private String criarNota(String token, String titulo) throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", titulo,
                                "content", "conteudo",
                                "biblicalReferences", List.of(),
                                "themeIds", List.of()))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asText();
    }

    @Test
    @DisplayName("POST devolve createdAt e updatedAt preenchidos")
    void postDevolveTimestamps() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Nota",
                                "content", "texto",
                                "biblicalReferences", List.of(),
                                "themeIds", List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE devolve 204 sem corpo")
    void deleteDevolve204() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");
        String id = criarNota(token, "Para apagar");

        String corpo = mockMvc.perform(delete("/api/v1/notes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).isEmpty();

        mockMvc.perform(get("/api/v1/notes/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("entidades HTML coladas sao convertidas na entrada")
    void desescapaHtmlColado() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Titulo",
                                "content", "&gt; citacao com &quot;aspas&quot;",
                                "biblicalReferences", List.of(),
                                "themeIds", List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("> citacao com \"aspas\""));
    }

    @Test
    @DisplayName("nota de outro usuario nao e acessivel (IDOR)")
    void naoAcessaNotaDeOutroUsuario() throws Exception {
        String tokenA = criarUsuario("a@exemplo.com", "39053344705");
        String idDeA = criarNota(tokenA, "Nota do A");

        String tokenB = criarUsuario("b@exemplo.com", "11122233396");

        mockMvc.perform(get("/api/v1/notes/" + idDeA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/notes/" + idDeA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // E continua existindo para o dono
        mockMvc.perform(get("/api/v1/notes/" + idDeA).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a listagem so traz as notas do proprio usuario")
    void listagemIsoladaPorUsuario() throws Exception {
        String tokenA = criarUsuario("a@exemplo.com", "39053344705");
        criarNota(tokenA, "Nota do A");

        String tokenB = criarUsuario("b@exemplo.com", "11122233396");

        mockMvc.perform(get("/api/v1/notes").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("plano FREE bloqueia a sexta nota com 403")
    void limiteDeNotasDoPlanoFree() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");

        for (int i = 1; i <= 5; i++) {
            criarNota(token, "Nota " + i);
        }

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Sexta",
                                "content", "texto",
                                "biblicalReferences", List.of(),
                                "themeIds", List.of()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("plano FREE bloqueia o quarto tema com 403")
    void limiteDeTemasDoPlanoFree() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/themes")
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON)
                            .content(json(Map.of("name", "Tema " + i))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/themes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("name", "Quarto"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UUID malformado devolve 400 e inexistente devolve 404")
    void idsInvalidos() throws Exception {
        String token = criarUsuario("a@exemplo.com", "39053344705");

        mockMvc.perform(get("/api/v1/notes/abc").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/notes/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
