package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.RegisterRequest;
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

import static org.junit.jupiter.api.Assertions.assertSame;
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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                appUserRepository,
                passwordEncoder,
                authenticationManager,
                jwtEncoder,
                900
        );
    }

    @Test
    void registerPropagatesDatabaseConstraintViolation() {
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

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> authService.register(request)
        );

        assertSame(databaseException, thrown);
    }
}
