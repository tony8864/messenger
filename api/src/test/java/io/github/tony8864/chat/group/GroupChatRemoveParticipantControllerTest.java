package io.github.tony8864.chat.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.group.dto.RemoveParticipantApiRequest;
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
import java.util.ArrayList;
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
public class GroupChatRemoveParticipantControllerTest {
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
    void shouldRemoveParticipantSuccessfully() throws Exception {
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

        // Create group chat with 3 participants (admin + 2 members)
        var participants = List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        );
        var chat = GroupChat.create(ChatId.newId(), participants, "My Group");
        groupChatRepository.save(chat);

        // Generate token for admin (the requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId anymore)
        var removeRequest = new RemoveParticipantApiRequest(
                chat.getChatId().getValue(),
                member1.getUserId().getValue()  // only user to remove
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/remove-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.groupName").value("My Group"))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(2)) // one removed
                .andExpect(jsonPath("$.participants[*].userId").value(
                        containsInAnyOrder(
                                admin.getUserId().getValue(),
                                member2.getUserId().getValue()
                        )
                ));
    }

    @Test
    void shouldFailWhenNonAdminTriesToRemove() throws Exception {
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

        // Create group chat with admin + 2 members
        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "My Group");
        groupChatRepository.save(chat);

        // Generate token for member1 (non-admin requester)
        var claims = new UserClaims(
                member1.getUserId().getValue(),
                member1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId anymore)
        var removeRequest = new RemoveParticipantApiRequest(
                chat.getChatId().getValue(),
                member2.getUserId().getValue() // target to remove
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/remove-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Only admins can manage participants"));
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
        var member1 = User.create(
                UserId.newId(),
                "member_" + UUID.randomUUID(),
                Email.of("member_" + UUID.randomUUID() + "@example.com"),
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

        // Use a random, non-existing chatId
        var fakeChatId = UUID.randomUUID().toString();

        // Generate token for admin (valid requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId anymore)
        var removeRequest = new RemoveParticipantApiRequest(
                fakeChatId,
                member1.getUserId().getValue() // valid target
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/remove-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_CHAT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenRequesterDoesNotExist() throws Exception {
        // --- Arrange ---
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
        var member3 = User.create(
                UserId.newId(),
                "member3_" + UUID.randomUUID(),
                Email.of("member3_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(member3);

        // Create group chat with a valid admin and two members
        var participants = new ArrayList<>(List.of(
                Participant.create(member1.getUserId(), Role.ADMIN),
                Participant.create(member2.getUserId(), Role.MEMBER),
                Participant.create(member3.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), new ArrayList<>(participants), "Group Chat");
        groupChatRepository.save(chat);

        // Fake requester (not in DB)
        var fakeRequesterId = UUID.randomUUID().toString();

        // Generate token for this non-existent user
        var claims = new UserClaims(fakeRequesterId, "ghost@example.com", Set.of("ADMIN"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId anymore)
        var removeRequest = new RemoveParticipantApiRequest(
                chat.getChatId().getValue(),
                member2.getUserId().getValue() // target to remove
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/remove-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenTargetUserDoesNotExist() throws Exception {
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

        // Create group chat with 3 valid users
        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "Group Chat");
        groupChatRepository.save(chat);

        // Fake remove target (not in DB)
        var fakeRemoveUserId = UUID.randomUUID().toString();

        // Generate token for admin (valid requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId anymore)
        var removeRequest = new RemoveParticipantApiRequest(
                chat.getChatId().getValue(),
                fakeRemoveUserId
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/remove-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
}
