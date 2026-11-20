package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.LoginRequest;
import com.paradoxdevs.grocery.api.request.RegisterRequest;
import com.paradoxdevs.grocery.api.response.AuthResponse;
import com.paradoxdevs.grocery.api.response.CurrentUserResponse;
import com.paradoxdevs.grocery.entity.AppUser;
import com.paradoxdevs.grocery.error.exception.DuplicateUsernameException;
import com.paradoxdevs.grocery.error.exception.InvalidAuthRequestException;
import com.paradoxdevs.grocery.error.exception.InvalidCredentialsException;
import com.paradoxdevs.grocery.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long tokenLifetimeSeconds;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-seconds}") long tokenLifetimeSeconds
    ) {
        if (tokenLifetimeSeconds <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_SECONDS must be greater than zero");
        }
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.tokenLifetimeSeconds = tokenLifetimeSeconds;
    }

    @Transactional
    public CurrentUserResponse register(RegisterRequest request) {
        String username = request.getUsername().strip();
        if (appUserRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException();
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return new CurrentUserResponse(appUserRepository.save(appUser).getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidAuthRequestException();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.getUsername().strip(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("grocery")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(tokenLifetimeSeconds))
                .subject(authentication.getName())
                .claim("scope", "ROLE_USER")
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
        return new AuthResponse(token, "Bearer", tokenLifetimeSeconds);
    }
}
