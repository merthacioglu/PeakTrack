package org.mhacioglu.peaktrackserver.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PasswordValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Violated constraints: too short, uppercase, digit, special char")
    void testInvalidPassword1() {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("testUsername")
                .password("pass")
                .build();

        Set<ConstraintViolation<RegisterUserDto>> violations =
                validator.validate(dto);

        Set<String> violationMessages = violations.stream().map(ConstraintViolation::getMessage)
                        .collect(Collectors.toSet());

        assertFalse(violations.isEmpty());
        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("uppercase")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("digit")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("special character")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("must be between")));
    }

    @Test
    @DisplayName("Violated constraints: too long, lowercase, digit, special char")
    void testInvalidPassword2() {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("testUsername")
                .password("THISISAREALLYLONGPASSWORD")
                .build();

        Set<ConstraintViolation<RegisterUserDto>> violations =
                validator.validate(dto);

        Set<String> violationMessages = violations.stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertFalse(violations.isEmpty());
        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("lowercase")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("digit")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("special character")));

        assertTrue(violationMessages.stream().
                anyMatch(m -> m.contains("must be between")));


    }

    @Test
    @DisplayName("No violated constraints")
    void testValidPasword() {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("testUsername")
                .password("v4l1dP4$$w0rd")
                .build();

        Set<ConstraintViolation<RegisterUserDto>> violations =
                validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

}
