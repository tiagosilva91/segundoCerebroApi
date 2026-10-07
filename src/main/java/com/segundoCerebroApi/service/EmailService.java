package com.segundoCerebroApi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Recuperação de Senha - Segundo Cérebro");
        
        // Em um cenário real, você apontaria para o URL do frontend de redefinição de senha
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        
        message.setText("Você solicitou a redefinição de sua senha.\n\n" +
                "Clique no link abaixo para alterar sua senha (válido por 1 hora):\n" +
                resetUrl + "\n\n" +
                "Se você não solicitou isso, pode ignorar este email.");
                
        mailSender.send(message);
    }
}
