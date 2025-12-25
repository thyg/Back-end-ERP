package com.rtcomops.treasury.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for IBAN (International Bank Account Number).
 *
 * <p>This annotation validates that a string is a valid IBAN according to:</p>
 * <ul>
 *   <li>ISO 13616-1:2007 format</li>
 *   <li>Country-specific length requirements</li>
 *   <li>MOD-97 checksum (ISO 7064)</li>
 * </ul>
 *
 * <p>Empty or null values are considered valid (IBAN is optional).
 * Use {@code @NotBlank} in addition if IBAN is required.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @ValidIban
 * private String iban;
 *
 * // Or with custom message:
 * @ValidIban(message = "L'IBAN fourni n'est pas valide")
 * private String iban;
 * }
 * </pre>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Documented
@Constraint(validatedBy = IbanConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIban {

    /**
     * The error message to display when validation fails.
     *
     * @return the error message
     */
    String message() default "Format IBAN invalide. Exemple: FR7630001007941234567890185 ou CM2110002000300277976315143";

    /**
     * Validation groups.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};
}
