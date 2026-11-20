package com.paradoxdevs.grocery.controller;

import com.paradoxdevs.grocery.api.request.LoginRequest;
import com.paradoxdevs.grocery.api.request.RegisterRequest;
import com.paradoxdevs.grocery.api.response.AuthResponse;
import com.paradoxdevs.grocery.api.response.CurrentUserResponse;
import com.paradoxdevs.grocery.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Authentication authentication) {
        return new CurrentUserResponse(authentication.getName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
