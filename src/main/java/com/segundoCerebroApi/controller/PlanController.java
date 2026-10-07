package com.segundoCerebroApi.controller;

import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.PlanUsageDTO;
import com.segundoCerebroApi.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plan")
@Tag(name = "Plano", description = "Uso e limites do plano do usuário")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/usage")
    @Operation(summary = "Uso atual do plano",
            description = "Retorna plano, quantidade usada, limite (null = ilimitado) e se pode criar mais notas/temas")
    public ResponseEntity<PlanUsageDTO> usage(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(planService.usage(user));
    }
}
