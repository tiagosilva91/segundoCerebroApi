package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.CheckoutResponseDTO;
import com.segundoCerebroApi.dto.PaymentWebhookDTO;
import com.segundoCerebroApi.dto.UserResponseDTO;
import com.segundoCerebroApi.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Pagamento", description = "Endpoints para checkout, webhook e upgrade de plano")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Inicia checkout do Plano PRO",
            description = "Gera uma URL de redirecionamento para o gateway de pagamento")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(user));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook do gateway de pagamento",
            description = "Recebe notificações de pagamento e atualiza o plano do usuário")
    public ResponseEntity<Void> webhook(@RequestBody PaymentWebhookDTO payload) {
        paymentService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/simulate-upgrade")
    @Operation(summary = "Simula upgrade imediato para plano PRO",
            description = "Permite testar o desbloqueio de funcionalidades sem gateway configurado")
    public ResponseEntity<UserResponseDTO> simulateUpgrade(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.simulateUpgrade(user));
    }
}
