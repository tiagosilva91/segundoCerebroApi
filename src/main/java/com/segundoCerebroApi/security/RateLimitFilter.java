package com.segundoCerebroApi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.segundoCerebroApi.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite de tentativas por IP nos endpoints públicos sensíveis.
 *
 * <p>{@code /auth/login} e {@code /auth/forgot-password} eram públicos e irrestritos:
 * o primeiro permitia força bruta de senha, o segundo transformava o SMTP da aplicação
 * em canal de spam e em oráculo para descobrir quais e-mails têm conta.</p>
 *
 * <p>A contagem é em memória, por instância. Com várias réplicas o limite efetivo é
 * multiplicado pelo número de instâncias — para um limite global seria preciso Redis.
 * Para o volume atual, a proteção em memória já elimina o ataque automatizado.</p>
 *
 * <p>Registrado explicitamente em {@code RateLimitConfig} e não como {@code @Component}:
 * um Filter anotado seria registrado também na cadeia do servlet pelo Spring Boot e
 * cada tentativa contaria duas vezes.</p>
 *
 * <p>O limite é por IP, não por e-mail: ler o corpo da requisição aqui consumiria o
 * stream antes do controller. Isso significa que um atacante com muitos IPs ainda
 * consegue distribuir tentativas contra uma conta; bloqueio por conta exigiria
 * persistir tentativas por usuário.</p>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String FORGOT_PATH = "/api/v1/auth/forgot-password";

    private final ObjectMapper objectMapper;

    private final int loginMaxAttempts;
    private final int forgotMaxAttempts;

    private final Cache<String, AtomicInteger> loginAttempts;
    private final Cache<String, AtomicInteger> forgotAttempts;

    public RateLimitFilter(
            ObjectMapper objectMapper,
            int loginMaxAttempts,
            int loginWindowMinutes,
            int forgotMaxAttempts,
            int forgotWindowMinutes) {

        this.objectMapper = objectMapper;
        this.loginMaxAttempts = loginMaxAttempts;
        this.forgotMaxAttempts = forgotMaxAttempts;

        this.loginAttempts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(loginWindowMinutes))
                .maximumSize(10_000)
                .build();

        this.forgotAttempts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(forgotWindowMinutes))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        Cache<String, AtomicInteger> cache;
        int max;
        String what;

        if (LOGIN_PATH.equals(path)) {
            cache = loginAttempts;
            max = loginMaxAttempts;
            what = "login";
        } else if (FORGOT_PATH.equals(path)) {
            cache = forgotAttempts;
            max = forgotMaxAttempts;
            what = "recuperação de senha";
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        String client = clientIp(request);
        int count = cache.get(client, k -> new AtomicInteger()).incrementAndGet();

        if (count > max) {
            log.warn("Rate limit atingido em {} para o IP {} ({} tentativas)", path, client, count);
            tooManyRequests(response,
                    "Muitas tentativas de " + what + ". Aguarde alguns minutos e tente novamente.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Com {@code server.forward-headers-strategy=framework}, o Spring já reescreve o
     * remote address a partir do X-Forwarded-For quando a aplicação está atrás de um
     * proxy reverso, então aqui basta o endereço da requisição.
     */
    private String clientIp(HttpServletRequest request) {
        String addr = request.getRemoteAddr();
        return addr == null ? "desconhecido" : addr;
    }

    private void tooManyRequests(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), message, LocalDateTime.now()));
    }
}
