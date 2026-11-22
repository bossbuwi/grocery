package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MethodArgumentNotValidExceptionHandler extends BaseExceptionHandler<MethodArgumentNotValidException> {

    public MethodArgumentNotValidExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        MethodArgumentNotValidException methodArgumentNotValidException =
                (MethodArgumentNotValidException) ex;
        log.warn(methodArgumentNotValidException.getMessage());

        ArrayList<String> fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));
        log.error(String.valueOf(fieldErrors));
        return errorResponseBuilder.build(
                request,
                ErrorCode.REQUEST_VALIDATION_ERROR,
                null,
                fieldErrors,
                null
        );
    }
}
