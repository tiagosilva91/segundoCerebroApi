package com.segundoCerebroApi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Valida um CPF pelos dígitos verificadores, não só pelo formato.
 *
 * <p>Antes o cadastro usava apenas {@code @Pattern(regexp = "\\d{11}")}, que
 * aceita "00000000000" e qualquer outra sequência de onze dígitos. Como o CPF
 * é campo único da conta, um valor inválido não dá para corrigir depois sem
 * mexer no banco.</p>
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RUNTIME)
public @interface Cpf {
    String message() default "CPF inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
