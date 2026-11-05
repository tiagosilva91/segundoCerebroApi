package com.segundoCerebroApi.dto;

import com.segundoCerebroApi.validation.Cpf;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Dados de cadastro e atualização de usuário.
 *
 * <p>As restrições aqui eram frouxas demais e deixavam entrar conta inutilizável
 * ou insegura:</p>
 * <ul>
 *   <li>{@code email} só tinha {@code @Email}, que pela especificação do Bean
 *       Validation considera {@code ""} válido. Uma conta de e-mail vazio
 *       transformava qualquer token inválido em sessão válida (ver
 *       {@code TokenService}), além de nunca conseguir fazer login.</li>
 *   <li>{@code password} só tinha {@code @NotBlank}, aceitando um caractere,
 *       enquanto troca e redefinição de senha já exigiam de 6 a 100.</li>
 *   <li>{@code birthDate} aceitava data no futuro.</li>
 *   <li>{@code cpf} só checava o formato, aceitando "00000000000".</li>
 * </ul>
 */
public record UserRequestDTO(
        @NotBlank @Size(max = 255) String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255)
        String email,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve ser no passado.")
        LocalDate birthDate,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String password,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos.")
        @Cpf
        String cpf
) {}
