package com.ezra.loanbackend.exceptions;

import com.ezra.loanbackend.dto.UniversalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoanException.class)
    public ResponseEntity<UniversalResponse> handleLoanException(LoanException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(UniversalResponse.error(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UniversalResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(UniversalResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<UniversalResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(UniversalResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<UniversalResponse> handleRestClient(RestClientResponseException ex) {
        String message = "Upstream service error: HTTP " + ex.getStatusCode().value();
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(UniversalResponse.error(HttpStatus.BAD_GATEWAY.value(), message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<UniversalResponse> handleResponseStatus(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(UniversalResponse.error(code, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UniversalResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(UniversalResponse.error(HttpStatus.BAD_REQUEST.value(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UniversalResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UniversalResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred"));
    }
}
