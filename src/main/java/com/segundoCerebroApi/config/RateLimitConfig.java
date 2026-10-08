package com.segundoCerebroApi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.segundoCerebroApi.security.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Registra o {@link RateLimitFilter} antes da cadeia do Spring Security, para que
 * as tentativas sejam contadas mesmo nas rotas públicas.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            ObjectMapper objectMapper, Environment env) {

        RateLimitFilter filter = new RateLimitFilter(
                objectMapper,
                env.getProperty("app.rate-limit.login.max-attempts", Integer.class, 10),
                env.getProperty("app.rate-limit.login.window-minutes", Integer.class, 5),
                env.getProperty("app.rate-limit.forgot-password.max-attempts", Integer.class, 3),
                env.getProperty("app.rate-limit.forgot-password.window-minutes", Integer.class, 15));

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/v1/auth/login", "/api/v1/auth/forgot-password");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
