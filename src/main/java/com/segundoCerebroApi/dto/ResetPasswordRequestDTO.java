package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDTO {
    @NotBlank(message = "O token não pode estar em branco")
    private String token;

    @NotBlank(message = "A nova senha não pode estar em branco")
    private String newPassword;
}
