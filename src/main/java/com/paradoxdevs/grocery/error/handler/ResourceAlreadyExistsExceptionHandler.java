package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class ResourceAlreadyExistsExceptionHandler
        extends BaseExceptionHandler<ResourceAlreadyExistsException> {

    public ResourceAlreadyExistsExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        ResourceAlreadyExistsException alreadyExistsException =
                (ResourceAlreadyExistsException) exception;
        log.warn(exception.getMessage());
        return errorResponseBuilder.build(
                request,
                alreadyExistsException.getErrorCode(),
                alreadyExistsException.getMessage(),
                null,
                null
        );
    }
}
