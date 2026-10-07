package com.segundoCerebroApi.service;

import com.segundoCerebroApi.domain.PasswordResetToken;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.repository.PasswordResetTokenRepository;
import com.segundoCerebroApi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Invalidate any existing tokens for this user
            tokenRepository.deleteByUser(user);

            // Generate a new token
            String token = UUID.randomUUID().toString();
            
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1 hour expiration
            
            tokenRepository.save(resetToken);

            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }
        // If user is not present, we do not throw an error to prevent email enumeration attacks
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove token after successful reset
        tokenRepository.deleteByUser(user);
        
        return true;
    }
}
