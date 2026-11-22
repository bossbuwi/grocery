package com.paradoxdevs.grocery.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(100, "Please login to access the resource."),
    UNAUTHORIZED(101, "User not authorized to access the resource."),
    MALFORMED_REQUEST(102, "Malformed JSON request, please check your request and try again."),
    REQUEST_VALIDATION_ERROR(103, "Request validation error."),
    INVALID_CREDENTIALS(104, "Username or password is incorrect."),
    RESOURCE_ALREADY_EXISTS(105, "The %s: %s already exists."),
    RESOURCE_NOT_FOUND(106, "The requested resource was not found."),
    RESOURCE_HAS_CHILDREN(107, "The requested resource still owns %s %s."),
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
