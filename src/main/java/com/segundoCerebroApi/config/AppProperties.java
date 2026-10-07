package com.segundoCerebroApi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades customizadas da aplicação (prefixo "app" no application.properties).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        Mail mail,
        Plan plan,
        Bible bible,
        Payment payment
) {
    public record Mail(String from) {}

    public record Plan(Limits free) {
        public record Limits(int maxNotes, int maxThemes) {}
    }

    public record Bible(String baseUrl, String defaultTranslation) {}

    public record Payment(String provider, long proPriceCents, String webhookSecret) {}
}
