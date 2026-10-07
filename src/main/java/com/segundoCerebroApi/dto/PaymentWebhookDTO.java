package com.segundoCerebroApi.dto;

public record PaymentWebhookDTO(
        String event,
        String userEmail,
        String status,
        String plan
) {}
