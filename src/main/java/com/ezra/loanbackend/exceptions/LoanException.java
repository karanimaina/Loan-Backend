package com.ezra.loanbackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Domain-specific failure for loan operations. Mapped by {@link GlobalExceptionHandler} to {@link com.ezra.loanbackend.dto.UniversalResponse}.
 */
public class LoanException extends RuntimeException {

    private final int status;

    public LoanException(int status, String message) {
        super(message);
        this.status = status;
    }

    public LoanException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public LoanException(HttpStatus status, String message) {
        this(status.value(), message);
    }

    public int getStatus() {
        return status;
    }

    public static LoanException notFound(String message) {
        return new LoanException(HttpStatus.NOT_FOUND, message);
    }

    public static LoanException badRequest(String message) {
        return new LoanException(HttpStatus.BAD_REQUEST, message);
    }

    public static LoanException conflict(String message) {
        return new LoanException(HttpStatus.CONFLICT, message);
    }
}
