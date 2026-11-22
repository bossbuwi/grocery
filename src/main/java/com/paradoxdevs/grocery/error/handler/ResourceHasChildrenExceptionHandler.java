package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.ResourceHasChildrenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class ResourceHasChildrenExceptionHandler extends BaseExceptionHandler<ResourceHasChildrenException> {

    public ResourceHasChildrenExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        ResourceHasChildrenException hasChildrenException = (ResourceHasChildrenException) exception;
        log.warn(hasChildrenException.getMessage());
        return errorResponseBuilder.build(
                request,
                hasChildrenException.getErrorCode(),
                hasChildrenException.getMessage(),
                null,
                null
        );
    }
}
