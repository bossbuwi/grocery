package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.RegisterRequest;
import com.paradoxdevs.grocery.utils.UniqueConstraintViolationTranslator;
import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import com.paradoxdevs.grocery.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    private UniqueConstraintViolationTranslator uniqueConstraintViolationTranslator;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        uniqueConstraintViolationTranslator = new UniqueConstraintViolationTranslator();
        authService = new AuthService(
                appUserRepository,
                passwordEncoder,
                authenticationManager,
                jwtEncoder,
                uniqueConstraintViolationTranslator,
                900
        );
    }

    @Test
    void registerTranslatesPostgresUniqueViolationIntoResourceAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("StrongPassword123");
        DataIntegrityViolationException databaseException = new DataIntegrityViolationException(
                "duplicate username",
                new SQLException(
                        "ERROR: duplicate key value violates unique constraint \"uk_app_users_username\"\n"
                                + "Detail: Key (username)=(user) already exists.",
                        "23505"
                )
        );
        when(appUserRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123")).thenReturn("encoded");
        when(appUserRepository.saveAndFlush(any())).thenThrow(databaseException);

        ResourceAlreadyExistsException thrown = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("The username: user already exists.", thrown.getMessage());
    }
}
