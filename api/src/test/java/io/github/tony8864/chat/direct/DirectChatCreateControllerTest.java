package io.github.tony8864.chat.direct;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.direct.dto.CreateDirectChatApiRequest;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.user.*;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
public class DirectChatCreateControllerTest {
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
    @Autowired private GroupChatRepository groupChatRepository;
    @Autowired private PasswordHasher passwordHasher;
    @Autowired private TokenService tokenService;

    @Test
    void shouldCreateDirectChatSuccessfully() throws Exception {
        // --- Arrange ---
        var user1 = User.create(
                UserId.newId(),
                "user1_" + UUID.randomUUID(),
                Email.of("user1_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var user2 = User.create(
                UserId.newId(),
                "user2_" + UUID.randomUUID(),
                Email.of("user2_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(user1);
        userRepository.save(user2);

        // Generate JWT token for user1 (requester)
        var claims = new UserClaims(
                user1.getUserId().getValue(),
                user1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request with only the "other" participant
        var createRequest = new CreateDirectChatApiRequest(user2.getUserId().getValue());

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/direct/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").isNotEmpty())
                .andExpect(jsonPath("$.participantIds").isArray())
                .andExpect(jsonPath("$.participantIds.length()").value(2))
                .andExpect(jsonPath("$.participantIds").value(
                        containsInAnyOrder(
                                user1.getUserId().getValue(), // from JWT
                                user2.getUserId().getValue()  // from request body
                        )
                ));
    }

    @Test
    void shouldReturnNotFoundWhenRequesterDoesNotExist() throws Exception {
        // --- Arrange ---
        var user2 = User.create(
                UserId.newId(),
                "user2_" + UUID.randomUUID(),
                Email.of("user2_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(user2);

        // Fake requesterId (not saved in DB)
        var fakeRequesterId = UUID.randomUUID().toString();

        // Generate JWT for this non-existent requester
        var claims = new UserClaims(
                fakeRequesterId,
                "ghost@example.com",
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var createRequest = new CreateDirectChatApiRequest(user2.getUserId().getValue());

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/direct/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenOtherUserDoesNotExist() throws Exception {
        // --- Arrange ---
        var requester = User.create(
                UserId.newId(),
                "requester_" + UUID.randomUUID(),
                Email.of("requester_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(requester);

        // Fake otherUserId (not saved in DB)
        var fakeOtherId = UUID.randomUUID().toString();

        // Generate JWT for valid requester
        var claims = new UserClaims(
                requester.getUserId().getValue(),
                requester.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var createRequest = new CreateDirectChatApiRequest(fakeOtherId);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/direct/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingChatWithYourself() throws Exception {
        // --- Arrange ---
        var requester = User.create(
                UserId.newId(),
                "self_" + UUID.randomUUID(),
                Email.of("self_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(requester);

        // Generate JWT for valid requester
        var claims = new UserClaims(
                requester.getUserId().getValue(),
                requester.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Request body incorrectly sets otherUserId = requesterId
        var createRequest = new CreateDirectChatApiRequest(requester.getUserId().getValue());

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/direct/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CHAT"))
                .andExpect(jsonPath("$.message").value("Cannot create a direct chat with yourself"));
    }
}
