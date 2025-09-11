package io.github.tony8864.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.*;
import io.github.tony8864.message.dto.SendMessageApiRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class MessageControllerTest {
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
    @Autowired private DirectChatRepository directChatRepository;
    @Autowired private PasswordHasher passwordHasher;
    @Autowired private TokenService tokenService;

    @Test
    void shouldSendMessageSuccessfully() throws Exception {
        // --- Arrange ---
        var sender = User.create(
                UserId.newId(),
                "sender_" + UUID.randomUUID(),
                Email.of("sender_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var receiver = User.create(
                UserId.newId(),
                "receiver_" + UUID.randomUUID(),
                Email.of("receiver_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(sender);
        userRepository.save(receiver);

        var chat = DirectChat.create(ChatId.newId(), List.of(sender.getUserId(), receiver.getUserId()));
        directChatRepository.save(chat);

        // Generate JWT for sender
        var claims = new UserClaims(
                sender.getUserId().getValue(),
                sender.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var sendRequest = new SendMessageApiRequest("Hello from integration test!");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.senderId").value(sender.getUserId().getValue()))
                .andExpect(jsonPath("$.content").value("Hello from integration test!"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldFailWhenSenderNotInDirectChat() throws Exception {
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
        var outsider = User.create(
                UserId.newId(),
                "outsider_" + UUID.randomUUID(),
                Email.of("outsider_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(outsider);

        DirectChat chat = DirectChat.create(ChatId.newId(), List.of(user1.getUserId(), user2.getUserId()));
        directChatRepository.save(chat);

        // JWT for outsider
        var claims = new UserClaims(
                outsider.getUserId().getValue(),
                outsider.getEmail().getValue(),
                Set.of("USER")
        );
        String outsiderToken = tokenService.generate(claims, Duration.ofHours(1));

        var sendRequest = new SendMessageApiRequest("Hello, I should not be allowed!");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/" + chat.getChatId().getValue() + "/messages")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sendRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldFailWhenSenderNotInGroupChat() throws Exception {
        // --- Arrange ---
        var admin = User.create(
                UserId.newId(),
                "admin_" + UUID.randomUUID(),
                Email.of("admin_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var member1 = User.create(
                UserId.newId(),
                "member1_" + UUID.randomUUID(),
                Email.of("member1_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var member2 = User.create(
                UserId.newId(),
                "member2_" + UUID.randomUUID(),
                Email.of("member2_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var outsider = User.create(
                UserId.newId(),
                "outsider_" + UUID.randomUUID(),
                Email.of("outsider_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(admin);
        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(outsider);

        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "Team Chat");
        groupChatRepository.save(chat);

        // JWT for outsider
        var claims = new UserClaims(
                outsider.getUserId().getValue(),
                outsider.getEmail().getValue(),
                Set.of("USER")
        );
        String outsiderToken = tokenService.generate(claims, Duration.ofHours(1));

        var sendRequest = new SendMessageApiRequest("Hello, I should not be allowed!");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/" + chat.getChatId().getValue() + "/messages")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sendRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").exists());
    }
}