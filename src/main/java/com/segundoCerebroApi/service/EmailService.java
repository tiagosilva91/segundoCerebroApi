package com.segundoCerebroApi.service;

import com.segundoCerebroApi.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties props;

    public EmailService(JavaMailSender mailSender, AppProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String baseUrl = props.frontendUrl() != null ? props.frontendUrl() : "http://localhost:3000";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (props.mail() != null && props.mail().from() != null) {
                message.setFrom(props.mail().from());
            }
            message.setTo(toEmail);
            message.setSubject("Recuperação de Senha - Segundo Cérebro do Pregador");
            message.setText("Olá,\n\n" +
                    "Você solicitou a redefinição de sua senha no Acervo (Segundo Cérebro do Pregador).\n\n" +
                    "Acesse o link abaixo para criar uma nova senha (link válido por 1 hora):\n" +
                    resetUrl + "\n\n" +
                    "Caso não tenha solicitado, por favor ignore esta mensagem.");

            mailSender.send(message);
            log.info("E-mail de recuperação de senha enviado com sucesso para: {}", toEmail);
        } catch (Exception e) {
            log.warn("Não foi possível enviar e-mail via SMTP ({}), mas o token gerado para testes é: {}", e.getMessage(), token);
        }
    }
}
