package com.paradoxdevs.grocery.error;

import com.paradoxdevs.grocery.api.response.ErrorResponse;
import com.paradoxdevs.grocery.error.handler.DataIntegrityViolationExceptionHandler;
import com.paradoxdevs.grocery.utils.UniqueConstraintViolationTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataIntegrityViolationExceptionHandlerTest {
    private ErrorResponseFactory errorResponseFactory;

    @BeforeEach
    void setUp() {
        ErrorResponseBuilder errorResponseBuilder = new ErrorResponseBuilder();
        DataIntegrityViolationExceptionHandler handler = new DataIntegrityViolationExceptionHandler(
                errorResponseBuilder,
                new UniqueConstraintViolationTranslator()
        );
        errorResponseFactory = new ErrorResponseFactory(List.of(handler), errorResponseBuilder);
    }

    @Test
    void translatesUniqueViolationToResourceAlreadyExistsResponse() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate key",
                new SQLException(
                        "ERROR: duplicate key value violates unique constraint \"uk_app_users_username\"\n"
                                + "Detail: Key (username)=(user) already exists.",
                        "23505"
                )
        );

        ResponseEntity<ErrorResponse> response = errorResponseFactory.buildResponseEntity(
                exception,
                webRequest()
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), response.getBody().getCode());
        assertEquals("The username: user already exists.", response.getBody().getMessage());
    }

    @Test
    void returnsGenericInternalErrorForUnrecognizedIntegrityViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "foreign key violation",
                new SQLException("foreign key violation", "23503")
        );

        ResponseEntity<ErrorResponse> response = errorResponseFactory.buildResponseEntity(
                exception,
                webRequest()
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Unexpected internal error", response.getBody().getMessage());
    }

    private ServletWebRequest webRequest() {
        return new ServletWebRequest(new MockHttpServletRequest("POST", "/auth/register"));
    }
}
