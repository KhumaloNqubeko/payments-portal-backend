package com.bank.paymentsportal.exception;

import com.bank.paymentsportal.dto.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApi(ApiException exception) {
        return buildResponse(exception.getStatus(), exception.getMessage(), List.of());
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleJwt(Exception exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return buildResponse(HttpStatus.FORBIDDEN, "You are not allowed to access this resource", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details
        ));
    }
}
