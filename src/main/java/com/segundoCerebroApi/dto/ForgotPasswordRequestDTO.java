package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "O email não pode estar em branco")
    @Email(message = "Email inválido")
    private String email;
}
