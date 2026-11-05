package com.segundoCerebroApi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementa o cálculo oficial dos dois dígitos verificadores do CPF.
 *
 * <p>Nulo e branco passam: quem decide obrigatoriedade é {@code @NotBlank},
 * para a mensagem de erro apontar o problema certo.</p>
 */
public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;

        String digits = value.replaceAll("\\D", "");
        if (digits.length() != 11) return false;

        // Sequências repetidas ("111.111.111-11") satisfazem o cálculo dos
        // dígitos verificadores, então precisam ser barradas à parte.
        if (digits.chars().distinct().count() == 1) return false;

        int primeiro = digitoVerificador(digits, 9, 10);
        int segundo = digitoVerificador(digits, 10, 11);

        return primeiro == Character.getNumericValue(digits.charAt(9))
                && segundo == Character.getNumericValue(digits.charAt(10));
    }

    /** Soma os {@code tamanho} primeiros dígitos com pesos decrescentes a partir de {@code pesoInicial}. */
    private static int digitoVerificador(String digits, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += Character.getNumericValue(digits.charAt(i)) * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
