package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class InvalidCredentialsExceptionHandler extends BaseExceptionHandler<InvalidCredentialsException> {

    public InvalidCredentialsExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof InvalidCredentialsException ice) {
            log.warn(ice.getMessage());
            return errorResponseBuilder.build(request, ice.getErrorCode(), null, null, null);
        }

        log.warn("InvalidCredentialsExceptionHandler received an unexpected exception: {}", ex.getClass().getSimpleName());
        return errorResponseBuilder.build(request);
    }
}
