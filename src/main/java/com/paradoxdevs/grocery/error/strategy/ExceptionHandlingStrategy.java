package com.paradoxdevs.grocery.error.strategy;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public interface ExceptionHandlingStrategy {
    ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request);
    Class<? extends Exception> getSupportedException();
}
