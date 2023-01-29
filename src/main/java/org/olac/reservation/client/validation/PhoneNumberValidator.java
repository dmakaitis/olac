package org.olac.reservation.client.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final String PATTERN = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isBlank(value) || value.matches(PATTERN);
    }

}
