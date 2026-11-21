package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class ResourceAlreadyExistsException extends ApiException{

    public ResourceAlreadyExistsException(String field, String value) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                ErrorCode.RESOURCE_ALREADY_EXISTS.formatMessage(field, value)
        );
    }
}
