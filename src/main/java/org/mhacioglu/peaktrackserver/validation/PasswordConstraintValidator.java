package org.mhacioglu.peaktrackserver.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;


    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        List<String> violations = new ArrayList<>();

        // Check length
        if (password.length() < minLength || password.length() > maxLength) {
            violations.add(String.format(
                    "Password must be between %d and %d characters",
                    minLength, maxLength));
        }

        // Check for uppercase
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one uppercase letter");
        }

        // Check for lowercase
        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one lowercase letter");
        }

        // Check for digits
        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one digit");
        }

        // Check for special characters
        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one special character");
        }

        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            violations.forEach(violation ->
                    context.buildConstraintViolationWithTemplate(violation)
                            .addConstraintViolation()
            );
            return false;
        }

        return true;
    }
}
