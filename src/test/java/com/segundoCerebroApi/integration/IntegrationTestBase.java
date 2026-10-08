package com.segundoCerebroApi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.segundoCerebroApi.repository.NoteRepository;
import com.segundoCerebroApi.repository.ThemeRepository;
import com.segundoCerebroApi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base dos testes de integração.
 *
 * <p>Os testes rodam contra um MySQL real, não contra H2: as migrations Flyway usam
 * sintaxe específica de MySQL, e a colação das tabelas — origem da falha que
 * derrubou a aplicação em 2026-10-08 — só pode ser exercitada em MySQL.</p>
 *
 * <p>O banco é fornecido pelo ambiente, não por Testcontainers. Localmente basta o
 * {@code docker compose up -d mysqldb} que já se usa no dia a dia; no CI, um service
 * container. Isso mantém a suíte rápida e sem exigir acesso ao daemon do Docker a
 * partir da JVM.</p>
 *
 * <p>O schema usado é <strong>segundocerebro_test</strong>, separado do banco de
 * desenvolvimento: cada teste limpa as tabelas em {@link #limparBase()}, e apontar
 * isso para o banco de trabalho apagaria as notas reais. A URL está em
 * {@code application-test.properties} e não deve ser alterada para o banco de dev.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected NoteRepository noteRepository;
    @Autowired protected ThemeRepository themeRepository;

    @BeforeEach
    void limparBase() {
        noteRepository.deleteAll();
        themeRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
