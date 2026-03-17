package com.matias.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class AlphanumericWithWhiteSpacesValidator implements ConstraintValidator<AlphanumericWithWhiteSpaces, String> {

    private static final String REGEXP = "^[\\p{L}0-9]+[\\p{L}0-9\\s]*$";

    @Override
    public void initialize(AlphanumericWithWhiteSpaces constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        Pattern pattern = Pattern.compile(REGEXP);
        return StringUtils.hasText(value) && pattern.matcher(value).matches();
    }
}
