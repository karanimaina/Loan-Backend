package com.ezra.loanbackend.dto;

import org.springframework.http.HttpStatus;

/**
 * Standard API envelope for success and error payloads.
 */
public record UniversalResponse(int status, String message, Object data) {

    public static UniversalResponse success(Object data) {
        return new UniversalResponse(HttpStatus.OK.value(), "Success", data);
    }

    public static UniversalResponse success(String message, Object data) {
        return new UniversalResponse(HttpStatus.OK.value(), message, data);
    }

    public static UniversalResponse success(HttpStatus httpStatus, String message, Object data) {
        return new UniversalResponse(httpStatus.value(), message, data);
    }

    public static UniversalResponse error(int status, String message) {
        return new UniversalResponse(status, message, null);
    }

    public static UniversalResponse error(int status, String message, Object data) {
        return new UniversalResponse(status, message, data);
    }
}
