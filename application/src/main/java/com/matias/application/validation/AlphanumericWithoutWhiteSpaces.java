package com.matias.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AlphanumericWithoutWhiteSpacesValidator.class)
public @interface AlphanumericWithoutWhiteSpaces {

    String message() default "Debe contener solo caracteres alfanuméricos sin espacios";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
