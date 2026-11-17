package com.paradoxdevs.grocery.error;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ErrorResponseBuilder {

    public ResponseEntity<ErrorResponse> build(WebRequest webRequest,
                                               ErrorCode errorCode,
                                               String customMessage,
                                               List<String> inputFieldErrors,
                                               Object[] args) {
        String message = Optional.ofNullable(customMessage)
                .filter(msg -> !msg.isBlank())
                .orElseGet(() -> errorCode.formatMessage(args));

        ArrayList<String> fieldErrors = Optional.ofNullable(inputFieldErrors)
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .orElse(null);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(errorResponse, getStatus(errorCode));
    }

    private HttpStatus getStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
