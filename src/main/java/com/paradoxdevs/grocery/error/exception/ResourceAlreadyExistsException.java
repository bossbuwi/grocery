package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class ResourceAlreadyExistsException extends ApiException{

    public ResourceAlreadyExistsException() {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS);
    }
}
