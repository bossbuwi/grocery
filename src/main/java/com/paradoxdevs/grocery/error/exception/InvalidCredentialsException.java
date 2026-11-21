package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
