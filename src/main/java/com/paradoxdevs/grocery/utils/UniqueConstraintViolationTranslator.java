package com.paradoxdevs.grocery.utils;

import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UniqueConstraintViolationTranslator {
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";
    private static final Pattern POSTGRES_UNIQUE_DETAIL =
            Pattern.compile("Detail: Key \\(([^)]+)\\)=\\((.*?)\\) already exists\\.", Pattern.DOTALL);

    public Optional<ResourceAlreadyExistsException> translate(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                String message = sqlException.getMessage();
                if (message != null) {
                    Matcher detail = POSTGRES_UNIQUE_DETAIL.matcher(message);
                    if (detail.find()) {
                        return Optional.of(new ResourceAlreadyExistsException(
                                detail.group(1),
                                detail.group(2)
                        ));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
