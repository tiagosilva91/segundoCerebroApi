package com.segundoCerebroApi.dto;

import com.segundoCerebroApi.domain.PlanType;

/**
 * Uso atual do plano. "limit" nulo significa ilimitado (PRO).
 */
public record PlanUsageDTO(PlanType planType, Usage notes, Usage themes) {
    public record Usage(long used, Integer limit, boolean canCreate) {}
}
