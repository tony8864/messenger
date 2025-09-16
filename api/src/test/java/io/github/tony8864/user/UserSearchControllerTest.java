package io.github.tony8864.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PasswordHash;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.github.tony8864.user.dto.SearchUserApiRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
public class UserSearchControllerTest {
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

    @Autowired
    private TokenService tokenService;

    @Test
    void shouldFindUserSuccessfully() throws Exception {
        // --- Arrange ---
        var requester = User.create(
                UserId.newId(),
                "requester_" + UUID.randomUUID(),
                Email.of("requester_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var target = User.create(
                UserId.newId(),
                "target_" + UUID.randomUUID(),
                Email.of("target_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(requester);
        userRepository.save(target);

        // Generate JWT for requester
        var claims = new UserClaims(
                requester.getUserId().getValue(),
                requester.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var searchRequest = new SearchUserApiRequest(target.getUsername());

        // --- Act & Assert ---
        mockMvc.perform(post("/api/users/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(target.getUserId().getValue()))
                .andExpect(jsonPath("$.username").value(target.getUsername()));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        // --- Arrange ---
        var requester = User.create(
                UserId.newId(),
                "requester_" + UUID.randomUUID(),
                Email.of("requester_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(requester);

        // Generate JWT for requester
        var claims = new UserClaims(
                requester.getUserId().getValue(),
                requester.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Username that does not exist
        var searchRequest = new SearchUserApiRequest("missing_user_" + UUID.randomUUID());

        // --- Act & Assert ---
        mockMvc.perform(post("/api/users/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        // --- Arrange ---
        var requester = User.create(
                UserId.newId(),
                "requester_" + UUID.randomUUID(),
                Email.of("requester_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(requester);

        // Fake/invalid JWT
        String invalidToken = "this.is.not.a.valid.jwt";

        var searchRequest = new SearchUserApiRequest("whatever");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/users/search")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isUnauthorized());
    }
}
