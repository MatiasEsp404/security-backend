package com.matias.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class NumericWithoutWhiteSpacesValidator implements ConstraintValidator<NumericWithoutWhiteSpaces, String> {

    private static final String REGEXP = "^[0-9]+$";

    @Override
    public void initialize(NumericWithoutWhiteSpaces constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        Pattern pattern = Pattern.compile(REGEXP);
        return StringUtils.hasText(value) && pattern.matcher(value).matches();
    }
}
