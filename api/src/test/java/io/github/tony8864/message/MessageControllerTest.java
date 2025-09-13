package io.github.tony8864.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.*;
import io.github.tony8864.message.dto.SendMessageApiRequest;
import io.github.tony8864.message.repository.MessageRepository;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Autowired private MessageRepository messageRepository;
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

    @Test
    void shouldListMessagesForDirectChat() throws Exception {
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

        var chat = DirectChat.create(ChatId.newId(), List.of(user1.getUserId(), user2.getUserId()));
        directChatRepository.save(chat);

        // create some messages (assume repo saves them directly)
        Message m1 = Message.create(
                MessageId.newId(),
                chat.getChatId(),
                user1.getUserId(),
                "Hello user2!"
        );
        Message m2 = Message.create(
                MessageId.newId(),
                chat.getChatId(),
                user2.getUserId(),
                "Hey user1"
        );
        messageRepository.save(m1);
        messageRepository.save(m2);

        // JWT for user1
        var claims = new UserClaims(
                user1.getUserId().getValue(),
                user1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[0].messageId").isNotEmpty())
                .andExpect(jsonPath("$.messages[0].content").value("Hey user1"))
                .andExpect(jsonPath("$.messages[1].content").value("Hello user2!"));
    }

    @Test
    void shouldListMessagesForGroupChat() throws Exception {
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

        userRepository.save(admin);
        userRepository.save(member1);
        userRepository.save(member2);

        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "Test Group");
        groupChatRepository.save(chat);

        // create messages with slightly different timestamps
        Message m1 = Message.create(
                MessageId.newId(),
                chat.getChatId(),
                admin.getUserId(),
                "Welcome to the group"
        );
        messageRepository.save(m1);

        Thread.sleep(5);

        Message m2 = Message.create(
                MessageId.newId(),
                chat.getChatId(),
                member1.getUserId(),
                "Hello everyone"
        );
        messageRepository.save(m2);

        Thread.sleep(5);

        Message m3 = Message.create(
                MessageId.newId(),
                chat.getChatId(),
                member2.getUserId(),
                "Glad to be here"
        );
        messageRepository.save(m3);

        // JWT for member1
        var claims = new UserClaims(
                member1.getUserId().getValue(),
                member1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[0].content").value("Glad to be here"))
                .andExpect(jsonPath("$.messages[1].content").value("Hello everyone"))
                .andExpect(jsonPath("$.messages[2].content").value("Welcome to the group"));
    }

    @Test
    void shouldLimitMessagesReturned() throws Exception {
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

        var chat = DirectChat.create(ChatId.newId(), List.of(user1.getUserId(), user2.getUserId()));
        directChatRepository.save(chat);

        // three messages
        Message m1 = Message.create(MessageId.newId(), chat.getChatId(), user1.getUserId(), "Message 1");
        messageRepository.save(m1);

        Thread.sleep(5);

        Message m2 = Message.create(MessageId.newId(), chat.getChatId(), user2.getUserId(), "Message 2");
        messageRepository.save(m2);

        Thread.sleep(5);

        Message m3 = Message.create(MessageId.newId(), chat.getChatId(), user1.getUserId(), "Message 3");
        messageRepository.save(m3);

        // JWT for user1
        var claims = new UserClaims(
                user1.getUserId().getValue(),
                user1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .param("limit", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("Message 3"))
                .andExpect(jsonPath("$.messages[1].content").value("Message 2"));
    }

    @Test
    void shouldFailWhenUserNotInDirectChat() throws Exception {
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

        var chat = DirectChat.create(ChatId.newId(), List.of(user1.getUserId(), user2.getUserId()));
        directChatRepository.save(chat);

        // add a message so chat isn't empty
        Message message = Message.create(MessageId.newId(), chat.getChatId(), user1.getUserId(), "Hello user2");
        messageRepository.save(message);

        // JWT for outsider
        var claims = new UserClaims(
                outsider.getUserId().getValue(),
                outsider.getEmail().getValue(),
                Set.of("USER")
        );
        String outsiderToken = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldFailWhenUserNotInGroupChat() throws Exception {
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
        var chat = GroupChat.create(ChatId.newId(), participants, "Project Group");
        groupChatRepository.save(chat);

        // add a message
        Message message = Message.create(MessageId.newId(), chat.getChatId(), admin.getUserId(), "Group message");
        messageRepository.save(message);

        // JWT for outsider
        var claims = new UserClaims(
                outsider.getUserId().getValue(),
                outsider.getEmail().getValue(),
                Set.of("USER")
        );
        String outsiderToken = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", chat.getChatId().getValue())
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldFailWhenChatNotFound() throws Exception {
        // --- Arrange ---
        var user = User.create(
                UserId.newId(),
                "user_" + UUID.randomUUID(),
                Email.of("user_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(user);

        // generate a random chatId that does not exist
        String nonExistentChatId = UUID.randomUUID().toString();

        // JWT for user
        var claims = new UserClaims(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/chats/{chatId}/messages", nonExistentChatId)
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_CHAT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
}