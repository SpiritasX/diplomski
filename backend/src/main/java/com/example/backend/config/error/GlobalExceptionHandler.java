package com.example.backend.config.error;

import com.example.backend.shared.error.BusinessRuleViolationException;
import com.example.backend.shared.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                "urn:problem:resource-not-found",
                "Resource not found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ProblemDetail handleBusinessRuleViolation(
            BusinessRuleViolationException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "urn:problem:business-rule-violation",
                "Business rule violation",
                exception.getMessage(),
                request
        );
    }

    // AuthenticationManager.authenticate(...) in /auth/login executes inside MVC, not inside the Security filter chain.
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.UNAUTHORIZED,
                "urn:problem:authentication-required",
                "Authentication required",
                "Authentication failed.",
                request
        );
    }

    // Important because @PreAuthorize may throw AccessDeniedException during an MVC request.
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.FORBIDDEN,
                "urn:problem:access-denied",
                "Access denied",
                "You do not have permission to perform this operation.",
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid."
        );

        problem.setType(URI.create("urn:problem:validation-failed"));
        problem.setTitle("Validation failed");
        problem.setInstance(requestUri(request));

        var errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNullElse(
                                error.getDefaultMessage(),
                                "Invalid value"
                        )
                ))
                .toList();

        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request body is missing or malformed."
        );

        problem.setType(URI.create("urn:problem:malformed-request"));
        problem.setTitle("Malformed request");
        problem.setInstance(requestUri(request));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "urn:problem:internal-server-error",
                "Internal server error",
                "An unexpected error occurred.",
                request
        );
    }

    private ProblemDetail problem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }

    private URI requestUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return URI.create(
                    servletWebRequest
                            .getRequest()
                            .getRequestURI()
            );
        }

        return URI.create("/");
    }
}