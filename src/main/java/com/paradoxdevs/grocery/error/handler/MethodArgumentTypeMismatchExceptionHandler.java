package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@Component
public class MethodArgumentTypeMismatchExceptionHandler
        extends BaseExceptionHandler<MethodArgumentTypeMismatchException> {

    public MethodArgumentTypeMismatchExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        MethodArgumentTypeMismatchException mismatchException =
                (MethodArgumentTypeMismatchException) ex;
        Class<?> requiredType = mismatchException.getRequiredType();
        String expectedType = requiredType == null ? "the required type" : requiredType.getSimpleName();
        String fieldError = mismatchException.getName() + ": must be a valid " + expectedType;

        log.warn("Request parameter type mismatch: {}", fieldError);
        return errorResponseBuilder.build(
                request,
                ErrorCode.REQUEST_VALIDATION_ERROR,
                null,
                List.of(fieldError),
                null
        );
    }
}
