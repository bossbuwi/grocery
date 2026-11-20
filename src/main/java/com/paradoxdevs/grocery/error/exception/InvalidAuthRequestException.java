package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class InvalidAuthRequestException extends ApiException {

    public InvalidAuthRequestException() {
        super(ErrorCode.REQUEST_VALIDATION_ERROR);
    }
}
