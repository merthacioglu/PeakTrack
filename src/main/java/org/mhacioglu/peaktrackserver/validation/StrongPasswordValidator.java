package org.mhacioglu.peaktrackserver.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (password.length() < 6 || password.length() > 20) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be between 6 and 20 characters"
            ).addConstraintViolation();
            isValid = false;
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            isValid = false;
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            isValid = false;
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character"
            ).addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
