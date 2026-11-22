package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class HttpMessageNotReadableExceptionHandler
        extends BaseExceptionHandler<HttpMessageNotReadableException> {

    public HttpMessageNotReadableExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        log.warn("Request body could not be deserialized");
        return errorResponseBuilder.build(
                request,
                ErrorCode.MALFORMED_REQUEST,
                null,
                null,
                null
        );
    }
}
