package com.segundoCerebroApi.dto;

import com.segundoCerebroApi.domain.PlanType;

public record LoginResponseDTO(String token, boolean firstLogin, PlanType planType) {
}
