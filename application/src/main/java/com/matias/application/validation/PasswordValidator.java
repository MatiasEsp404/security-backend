package com.matias.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";

    @Override
    public void initialize(Password constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        Pattern pattern = Pattern.compile(REGEXP);
        return StringUtils.hasText(password) && pattern.matcher(password).matches();
    }
}
