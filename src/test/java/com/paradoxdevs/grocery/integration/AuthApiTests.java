package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.AppUser;
import com.paradoxdevs.grocery.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registersLogsInAndUsesBearerTokenForProtectedRoutes() throws Exception {
        register("grocery-user", "StrongPassword123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("grocery-user"));

        MvcResult loginResult = login("grocery-user", "StrongPassword123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn();

        JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = response.get("accessToken").asText();
        assertNotNull(token);
        assertTrue(loginResult.getResponse().getCookies().length == 0);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("grocery-user"));
        mockMvc.perform(get("/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidCredentialsAndDuplicateUsernames() throws Exception {
        register("grocery-user", "StrongPassword123").andExpect(status().isCreated());
        register("grocery-user", "StrongPassword123").andExpect(status().isConflict());
        login("grocery-user", "incorrect-password").andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsWeakAndOverlongUtf8Passwords() throws Exception {
        register("grocery-user", "short").andExpect(status().isBadRequest());
        register("grocery-user", "é".repeat(37)).andExpect(status().isBadRequest());
        register("  ", "StrongPassword123").andExpect(status().isBadRequest());
        register(" ab ", "StrongPassword123").andExpect(status().isBadRequest());
        register("u".repeat(51), "StrongPassword123").andExpect(status().isBadRequest());
        register("valid-user", "        ").andExpect(status().isBadRequest());
    }

    @Test
    void registrationStoresOnlyAnEncodedPassword() throws Exception {
        register("grocery-user", "StrongPassword123").andExpect(status().isCreated());

        AppUser appUser = appUserRepository.findByUsername("grocery-user").orElseThrow();
        assertNotEquals("StrongPassword123", appUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("StrongPassword123", appUser.getPasswordHash()));
    }

    @Test
    void rejectsMissingOrInvalidBearerTokens() throws Exception {
        mockMvc.perform(get("/items").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions register(String username, String password)
            throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password)));
    }

    private org.springframework.test.web.servlet.ResultActions login(String username, String password)
            throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password)));
    }
}
