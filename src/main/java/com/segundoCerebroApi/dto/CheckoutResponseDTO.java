package com.segundoCerebroApi.dto;

public record CheckoutResponseDTO(
        String checkoutUrl,
        String referenceId,
        long amountCents,
        String currency,
        String provider
) {}
