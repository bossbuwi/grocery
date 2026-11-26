package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.BasketQuantityLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class BasketQuantityLimitExceptionHandler extends BaseExceptionHandler<BasketQuantityLimitException> {
    public BasketQuantityLimitExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        BasketQuantityLimitException quantityLimitException = (BasketQuantityLimitException) exception;
        log.warn(quantityLimitException.getMessage());
        return errorResponseBuilder.build(
                request,
                quantityLimitException.getErrorCode(),
                quantityLimitException.getMessage(),
                null,
                null
        );
    }
}
