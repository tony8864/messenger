package io.github.tony8864.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.entities.user.*;
import io.github.tony8864.jwt.JwtTokenService;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.github.tony8864.user.dto.LoginApiRequest;
import io.github.tony8864.user.dto.RegisterUserApiRequest;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
public class UserAuthenticationControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TokenService tokenService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String uniqueEmail = "tony_" + UUID.randomUUID() + "@example.com";
        String uniqueUsername = "tony_" + UUID.randomUUID();

        // First register a user
        var registerRequest = new RegisterUserApiRequest(uniqueUsername, uniqueEmail, "secret123");
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Then login
        var loginRequest = new LoginApiRequest(uniqueEmail, "secret123");
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(uniqueUsername));
    }

    @Test
    void shouldFailLoginWithInvalidPassword() throws Exception {
        String uniqueEmail = "tony_" + UUID.randomUUID() + "@example.com";
        String uniqueUsername = "tony_" + UUID.randomUUID();

        // First register a user
        var registerRequest = new RegisterUserApiRequest(uniqueUsername, uniqueEmail, "secret123");
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Attempt login with wrong password
        var badLoginRequest = new LoginApiRequest(uniqueEmail, "wrongpassword");
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        var user = User.create(
                UserId.newId(),
                "user_" + UUID.randomUUID(),
                Email.of("user_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(user);

        var claims = new UserClaims(user.getUserId().getValue(), user.getEmail().getValue(), Set.of("USER"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User " + user.getUserId().getValue() + " logged out successfully."));
    }
}
