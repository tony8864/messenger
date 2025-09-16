package io.github.tony8864.chat.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.group.dto.RenameGroupChatApiRequest;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class GroupChatRenameControllerTest {
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
    void shouldRenameGroupChatSuccessfully() throws Exception {
        // --- Arrange ---
        // Create users
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

        // Create group chat
        var participants = List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        );
        var chat = GroupChat.create(ChatId.newId(), participants, "Original Name");
        groupChatRepository.save(chat);

        // Generate token for admin
        var claims = new UserClaims(admin.getUserId().getValue(), admin.getEmail().getValue(), Set.of("ADMIN"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var renameRequest = new RenameGroupChatApiRequest(
                chat.getChatId().getValue(),
                "New Group Name"
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.groupName").value("New Group Name"))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants[0].userId").isNotEmpty())
                .andExpect(jsonPath("$.participants[*].role").value(
                        containsInAnyOrder("ADMIN", "MEMBER", "MEMBER")
                ));

    }

    @Test
    void shouldReturnNotFoundWhenChatDoesNotExist() throws Exception {
        // --- Arrange ---
        var admin = User.create(
                UserId.newId(),
                "admin_" + UUID.randomUUID(),
                Email.of("admin_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        userRepository.save(admin);

        // Generate token for admin
        var claims = new UserClaims(admin.getUserId().getValue(), admin.getEmail().getValue(), Set.of("ADMIN"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Request with random chatId (does not exist)
        var renameRequest = new RenameGroupChatApiRequest(
                UUID.randomUUID().toString(),
                "Does Not Matter"
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_CHAT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldFailWhenNonAdminTriesToRename() throws Exception {
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

        // Create group chat with admin and members
        var participants = List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        );
        var chat = GroupChat.create(ChatId.newId(), participants, "Original Name");
        groupChatRepository.save(chat);

        // Generate token for non-admin (member1)
        var claims = new UserClaims(member1.getUserId().getValue(), member1.getEmail().getValue(), Set.of("USER"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var renameRequest = new RenameGroupChatApiRequest(
                chat.getChatId().getValue(),
                "Hacked Name"
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Only admin can rename group chat"));
    }

    @Test
    void shouldReturnNotFoundWhenRequesterDoesNotExist() throws Exception {
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

        // Create group chat
        var participants = List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        );
        var chat = GroupChat.create(ChatId.newId(), participants, "Original Name");
        groupChatRepository.save(chat);

        // Generate token for a random (nonexistent) user
        String nonExistentUserId = UUID.randomUUID().toString();
        var claims = new UserClaims(nonExistentUserId, "ghost@example.com", Set.of("ADMIN"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var renameRequest = new RenameGroupChatApiRequest(
                chat.getChatId().getValue(),
                "New Name"
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        var renameRequest = new RenameGroupChatApiRequest(UUID.randomUUID().toString(), "Some Name");

        mockMvc.perform(post("/api/chats/group/rename")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        // --- Arrange ---
        var renameRequest = new RenameGroupChatApiRequest(
                UUID.randomUUID().toString(),
                "Some Name"
        );

        // Send a fake/invalid token
        String invalidToken = "this.is.not.a.valid.jwt";

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenNewGroupNameIsBlank() throws Exception {
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

        // Create group chat
        var participants = List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        );
        var chat = GroupChat.create(ChatId.newId(), participants, "Original Name");
        groupChatRepository.save(chat);

        // Valid admin token
        var claims = new UserClaims(admin.getUserId().getValue(), admin.getEmail().getValue(), Set.of("ADMIN"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Blank group name
        var renameRequest = new RenameGroupChatApiRequest(
                chat.getChatId().getValue(),
                ""
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/rename")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isBadRequest());
    }
}