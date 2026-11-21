package com.paradoxdevs.grocery.error;

import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import com.paradoxdevs.grocery.utils.UniqueConstraintViolationTranslator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueConstraintViolationTranslatorTest {
    private final UniqueConstraintViolationTranslator translator = new UniqueConstraintViolationTranslator();

    @Test
    void extractsPostgresKeyAndValueFromUniqueViolation() {
        DataIntegrityViolationException databaseException = new DataIntegrityViolationException(
                "duplicate key",
                new SQLException(
                        "ERROR: duplicate key value violates unique constraint \"uk_app_users_username\"\n"
                                + "Detail: Key (username)=(user) already exists.",
                        "23505"
                )
        );

        ResourceAlreadyExistsException exception = translator.translate(databaseException).orElseThrow();

        assertEquals("The username: user already exists.", exception.getMessage());
    }

    @Test
    void doesNotTranslateOtherDatabaseViolations() {
        DataIntegrityViolationException databaseException = new DataIntegrityViolationException(
                "foreign key violation",
                new SQLException("foreign key violation", "23503")
        );

        assertTrue(translator.translate(databaseException).isEmpty());
    }

    @Test
    void doesNotTranslateUniqueViolationsWithoutPostgresKeyDetail() {
        DataIntegrityViolationException databaseException = new DataIntegrityViolationException(
                "duplicate key",
                new SQLException("duplicate key", "23505")
        );

        assertTrue(translator.translate(databaseException).isEmpty());
    }
}
