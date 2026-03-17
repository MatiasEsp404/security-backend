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
@Constraint(validatedBy = CharactersWithoutWhiteSpacesValidator.class)
public @interface CharactersWithoutWhiteSpaces {

    String message() default "Debe contener solo caracteres sin espacios";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
