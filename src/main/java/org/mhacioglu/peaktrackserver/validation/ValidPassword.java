package org.mhacioglu.peaktrackserver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface ValidPassword {
    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int minLength() default 6;
    int maxLength() default 20;
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecialChar() default true;
}
