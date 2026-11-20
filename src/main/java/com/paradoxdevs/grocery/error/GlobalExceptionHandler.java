package com.paradoxdevs.grocery.error;

import com.paradoxdevs.grocery.error.exception.DuplicateUsernameException;
import com.paradoxdevs.grocery.error.exception.InvalidAuthRequestException;
import com.paradoxdevs.grocery.error.exception.InvalidCredentialsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleInvalidRequest() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(InvalidAuthRequestException.class)
    public ResponseEntity<Void> handleInvalidAuthRequest() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<Void> handleDuplicateUsername() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Void> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
