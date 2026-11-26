package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.BasketItemNotPricedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class BasketItemNotPricedExceptionHandler extends BaseExceptionHandler<BasketItemNotPricedException> {
    public BasketItemNotPricedExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        BasketItemNotPricedException notPricedException = (BasketItemNotPricedException) exception;
        log.warn(notPricedException.getMessage());
        return errorResponseBuilder.build(
                request,
                notPricedException.getErrorCode(),
                notPricedException.getMessage(),
                null,
                null
        );
    }
}
