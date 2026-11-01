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
    /**
     * @param from remetente dos e-mails transacionais.
     * @param logResetTokenOnFailure imprime no log o token de recuperação quando
     *        o envio por SMTP falha. É conveniência de desenvolvimento e precisa
     *        continuar desligada em qualquer ambiente publicado: o token vale uma
     *        hora e redefine a senha, então quem lê o log assume a conta.
     */
    public record Mail(String from, boolean logResetTokenOnFailure) {}

    public record Plan(Limits free) {
        public record Limits(int maxNotes, int maxThemes) {}
    }

    public record Bible(String baseUrl, String defaultTranslation) {}

    public record Payment(
            String provider,
            long proPriceCents,
            String webhookSecret,
            boolean allowSimulatedUpgrade
    ) {}
}
