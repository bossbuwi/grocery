package com.paradoxdevs.grocery.error.handler;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.BaseExceptionHandler;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.error.ErrorResponseBuilder;
import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import com.paradoxdevs.grocery.utils.UniqueConstraintViolationTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Optional;

@Slf4j
@Component
public class DataIntegrityViolationExceptionHandler
        extends BaseExceptionHandler<DataIntegrityViolationException> {
    private final UniqueConstraintViolationTranslator uniqueConstraintViolationTranslator;

    public DataIntegrityViolationExceptionHandler(
            ErrorResponseBuilder errorResponseBuilder,
            UniqueConstraintViolationTranslator uniqueConstraintViolationTranslator
    ) {
        super(errorResponseBuilder);
        this.uniqueConstraintViolationTranslator = uniqueConstraintViolationTranslator;
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        log.warn(exception.getMessage(), exception);
        Optional<ResourceAlreadyExistsException> translatedException =
                uniqueConstraintViolationTranslator.translate((DataIntegrityViolationException) exception);
        if (translatedException.isPresent()) {
            ResourceAlreadyExistsException alreadyExistsException = translatedException.orElseThrow();
            log.warn(alreadyExistsException.getMessage());
            return errorResponseBuilder.build(
                    request,
                    alreadyExistsException.getErrorCode(),
                    alreadyExistsException.getMessage(),
                    null,
                    null
            );
        }

        log.error("Unrecognized database integrity violation; using internal server error response", exception);
        return errorResponseBuilder.build(
                request,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Unexpected internal error",
                null,
                null
        );
    }
}
