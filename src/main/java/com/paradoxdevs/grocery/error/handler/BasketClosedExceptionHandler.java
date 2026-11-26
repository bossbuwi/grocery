package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.BasketClosedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class BasketClosedExceptionHandler extends BaseExceptionHandler<BasketClosedException> {
    public BasketClosedExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        BasketClosedException basketClosedException = (BasketClosedException) exception;
        log.warn(basketClosedException.getMessage());
        return errorResponseBuilder.build(
                request,
                basketClosedException.getErrorCode(),
                basketClosedException.getMessage(),
                null,
                null
        );
    }
}
