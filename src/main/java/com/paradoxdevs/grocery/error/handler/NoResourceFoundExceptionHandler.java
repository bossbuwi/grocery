package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Component
public class NoResourceFoundExceptionHandler extends BaseExceptionHandler<NoResourceFoundException> {

    public NoResourceFoundExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        log.warn("No controller route or static resource found for {}", request.getDescription(false));
        return errorResponseBuilder.build(
                request,
                ErrorCode.RESOURCE_NOT_FOUND,
                null,
                null,
                null
        );
    }
}
