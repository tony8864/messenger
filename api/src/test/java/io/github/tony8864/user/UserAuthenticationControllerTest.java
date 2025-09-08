package io.github.tony8864.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PresenceStatus;
import io.github.tony8864.user.dto.LoginApiRequest;
import io.github.tony8864.user.dto.LogoutApiRequest;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

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
    void shouldLogoutSuccessfully() throws Exception {
        String uniqueEmail = "tony_" + UUID.randomUUID() + "@example.com";
        String uniqueUsername = "tony_" + UUID.randomUUID();

        // 1. Register the user
        var registerRequest = new RegisterUserApiRequest(
                uniqueUsername,
                uniqueEmail,
                "secret123"
        );

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Fetch the user from the DB
        var user = userRepository.findByEmail(Email.of(uniqueEmail))
                .orElseThrow(() -> new IllegalStateException("User not found after registration"));

        // 3. Logout the user
        var logoutRequest = new LogoutApiRequest(user.getUserId().getValue());
        mockMvc.perform(post("/api/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User " + user.getUserId().getValue() + " logged out successfully."));

        // 4. Verify DB state (user status updated to OFFLINE)
        var updated = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found after logout"));
        assertEquals(PresenceStatus.OFFLINE, updated.getStatus());
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
}
