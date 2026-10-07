package com.segundoCerebroApi.service;

import com.segundoCerebroApi.config.AppProperties;
import com.segundoCerebroApi.domain.PlanType;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.CheckoutResponseDTO;
import com.segundoCerebroApi.dto.PaymentWebhookDTO;
import com.segundoCerebroApi.dto.UserResponseDTO;
import com.segundoCerebroApi.exception.ResourceNotFoundException;
import com.segundoCerebroApi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final UserRepository userRepository;
    private final AppProperties props;

    public PaymentService(UserRepository userRepository, AppProperties props) {
        this.userRepository = userRepository;
        this.props = props;
    }

    /**
     * Cria sessão de checkout ou URL para redirecionamento.
     */
    public CheckoutResponseDTO createCheckoutSession(User user) {
        String referenceId = UUID.randomUUID().toString();
        long amountCents = props.payment() != null ? props.payment().proPriceCents() : 1990;
        String provider = props.payment() != null && props.payment().provider() != null
                ? props.payment().provider()
                : "mock";

        String frontendUrl = props.frontendUrl() != null ? props.frontendUrl() : "http://localhost:3000";
        if (frontendUrl.endsWith("/")) {
            frontendUrl = frontendUrl.substring(0, frontendUrl.length() - 1);
        }

        // URL de redirecionamento (pronta para substituição pela URL do checkout Stripe/MercadoPago quando integrado)
        String checkoutUrl = frontendUrl + "/app/settings?checkout=success&ref=" + referenceId;

        log.info("Sessão de pagamento criada para {}: provider={}, ref={}, checkoutUrl={}",
                user.getEmail(), provider, referenceId, checkoutUrl);

        return new CheckoutResponseDTO(checkoutUrl, referenceId, amountCents, "BRL", provider);
    }

    /**
     * Ativa plano PRO via webhook.
     */
    @Transactional
    public void processWebhook(PaymentWebhookDTO dto) {
        if (dto.userEmail() == null || dto.userEmail().isBlank()) {
            throw new IllegalArgumentException("userEmail é obrigatório no webhook.");
        }

        User user = userRepository.findByEmail(dto.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + dto.userEmail()));

        if ("paid".equalsIgnoreCase(dto.status()) || "approved".equalsIgnoreCase(dto.status())) {
            user.setPlanType(PlanType.PRO);
            userRepository.save(user);
            log.info("Usuário {} atualizado para PRO via webhook", user.getEmail());
        }
    }

    /**
     * Simula a ativação imediata do plano PRO (ideal para desenvolvimento/testes locais).
     */
    @Transactional
    public UserResponseDTO simulateUpgrade(User user) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        managedUser.setPlanType(PlanType.PRO);
        User saved = userRepository.save(managedUser);
        log.info("Plano do usuário {} promovido para PRO via simulação", saved.getEmail());
        return UserResponseDTO.fromEntity(saved);
    }
}
