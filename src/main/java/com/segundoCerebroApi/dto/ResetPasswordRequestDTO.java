package com.segundoCerebroApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDTO {
    /** Pode vir no body ou como query param (?token=...). */
    private String token;

    @NotBlank(message = "A nova senha não pode estar em branco")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
    private String newPassword;
}
