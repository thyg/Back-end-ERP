package com.rtcomops.treasury.validation;

import com.rtcomops.treasury.util.IbanValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Constraint validator implementation for the {@link ValidIban} annotation.
 *
 * <p>This validator uses the {@link IbanValidator} utility class to perform
 * comprehensive IBAN validation including format, length, and checksum checks.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public class IbanConstraintValidator implements ConstraintValidator<ValidIban, String> {

    /**
     * Initializes the validator.
     *
     * @param constraintAnnotation the annotation instance
     */
    @Override
    public void initialize(ValidIban constraintAnnotation) {
        // No initialization needed
    }

    /**
     * Validates the IBAN value.
     *
     * @param value the IBAN to validate
     * @param context the constraint validator context
     * @return true if the IBAN is valid or empty, false otherwise
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return IbanValidator.isValid(value);
    }
}
