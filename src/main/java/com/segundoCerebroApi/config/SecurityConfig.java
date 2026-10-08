package com.segundoCerebroApi.config;

import com.segundoCerebroApi.security.RestAuthenticationHandlers;
import com.segundoCerebroApi.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final RestAuthenticationHandlers authHandlers;
    private final List<String> allowedOrigins;
    private final boolean exposeApiDocs;

    public SecurityConfig(
            SecurityFilter securityFilter,
            RestAuthenticationHandlers authHandlers,
            @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
            List<String> allowedOrigins,
            @Value("${app.api-docs.public:false}") boolean exposeApiDocs) {
        this.securityFilter = securityFilter;
        this.authHandlers = authHandlers;
        this.allowedOrigins = allowedOrigins;
        this.exposeApiDocs = exposeApiDocs;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API Stateless
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users", "/api/v1/users/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bible/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment/webhook").permitAll()
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        // Swagger publico so quando app.api-docs.public=true (dev).
                        // Em producao o default false evita entregar o mapa da API.
                        .requestMatchers(apiDocsMatchers())
                            .access((auth, ctx) -> new AuthorizationDecision(exposeApiDocs))
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                // 401 para sessao ausente/expirada e 403 para falta de permissao,
                // ambos no formato ErrorResponse. O default do Spring devolvia 403
                // nos dois casos, impedindo o cliente de disparar logout automatico.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authHandlers)
                        .accessDeniedHandler(authHandlers)
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static RequestMatcher[] apiDocsMatchers() {
        return new RequestMatcher[]{
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/swagger-ui/**")
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // A lista vinha fixa no código e incluía "https://*.railway.app".
        // setAllowedOrigins não interpreta curinga, então aquela entrada nunca casava
        // com nada; trocá-la por setAllowedOriginPatterns, porém, liberaria qualquer
        // subdomínio railway.app — de qualquer dono — com allowCredentials ativo.
        // Agora as origens vêm de CORS_ALLOWED_ORIGINS, sem curinga.
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Webhook-Secret"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}