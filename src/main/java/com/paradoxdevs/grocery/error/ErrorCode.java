package com.paradoxdevs.grocery.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(100, "Please login to access the resource."),
    UNAUTHORIZED(101, "User not authorized to access the resource."),
    MALFORMED_REQUEST(100, "Malformed JSON request, please check your request and try again."),
    REQUEST_VALIDATION_ERROR(101, "Request validation error."),
    INTERNAL_SERVER_ERROR(999, "Something went wrong, please contact support."),;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }
}
