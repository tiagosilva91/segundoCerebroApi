package com.segundoCerebroApi.config;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.domain.UserRole;
import com.segundoCerebroApi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Garante a existência de um administrador quando as variáveis de ambiente são
 * fornecidas.
 *
 * <p>As rotas de gestão de usuários exigem {@code hasRole("ADMIN")}, mas
 * {@code User.role} nasce sempre {@code USER}, nenhuma migration semeava um
 * admin e não há endpoint para promover ninguém: na prática
 * {@code GET/PUT/DELETE /api/v1/users} eram inalcançáveis.</p>
 *
 * <p>Fica desligado por padrão. Para ligar, defina {@code ADMIN_EMAIL},
 * {@code ADMIN_PASSWORD} e {@code ADMIN_CPF} no ambiente. A senha só é aplicada
 * na criação — reiniciar a aplicação não sobrescreve a senha de um admin que já
 * existe, e uma conta comum já cadastrada com aquele e-mail é apenas promovida.</p>
 */
@Configuration
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    @Bean
    ApplicationRunner seedAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.cpf:}") String cpf,
            @Value("${app.admin.name:Administrador}") String name) {

        return args -> {
            if (email.isBlank() || password.isBlank()) {
                log.debug("Bootstrap de admin desligado (app.admin.email/password não definidos).");
                return;
            }
            try {
                criarOuPromover(userRepository, passwordEncoder, email, password, cpf, name);
            } catch (Exception e) {
                // Semear o admin é conveniência de operação: se falhar (um CPF já
                // usado por outra conta, por exemplo), a aplicação precisa subir
                // assim mesmo. Deixar a exceção escapar do ApplicationRunner
                // aborta o boot e tira a API inteira do ar.
                log.error("Não foi possível criar/promover o admin '{}': {}", email, e.getMessage());
            }
        };
    }

    @Transactional
    void criarOuPromover(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         String email, String password, String cpf, String name) {

        userRepository.findByEmail(email).ifPresentOrElse(
                existente -> {
                    if (existente.getRole() == UserRole.ADMIN) {
                        log.info("Admin '{}' já existe.", email);
                        return;
                    }
                    existente.setRole(UserRole.ADMIN);
                    userRepository.save(existente);
                    log.info("Usuário '{}' promovido a ADMIN.", email);
                },
                () -> {
                    if (cpf.isBlank()) {
                        log.warn("Admin '{}' não criado: ADMIN_CPF é obrigatório para uma conta nova.", email);
                        return;
                    }
                    if (userRepository.existsByCpf(cpf)) {
                        log.warn("Admin '{}' não criado: o CPF informado já pertence a outra conta.", email);
                        return;
                    }
                    User admin = new User();
                    admin.setName(name);
                    admin.setEmail(email);
                    admin.setCpf(cpf);
                    admin.setBirthDate(LocalDate.of(1970, 1, 1));
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setRole(UserRole.ADMIN);
                    // Força a troca da senha de bootstrap no primeiro acesso.
                    admin.setFirstLogin(true);
                    userRepository.save(admin);
                    log.info("Admin '{}' criado. Troque a senha no primeiro acesso.", email);
                });
    }
}
