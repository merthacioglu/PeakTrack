package org.mhacioglu.peaktrackserver.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {

        if (exception instanceof BadCredentialsException) {
            ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
            detail.setProperty("reason", "Username or password is incorrect.");
            return detail;

        }

        if (exception instanceof AccountStatusException ||
            exception instanceof  AccessDeniedException ||
            exception instanceof SignatureException ||
            exception instanceof ExpiredJwtException) {
            return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());

        }

        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());

    }

    @ExceptionHandler(WorkoutException.class)
    public ProblemDetail handleWorkoutException(WorkoutException ex) {
        if (ex instanceof WorkoutNotFoundException) {
            return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), ex.getMessage());
        }
        else {
            return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), ex.getMessage());
        }


    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleExistingUsernameException(UsernameAlreadyExistsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(409),
                exception.getMessage());

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400),
                "Validation error");

        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        errorDetail.setProperty("errors", errors);
        return errorDetail;
    }



}
