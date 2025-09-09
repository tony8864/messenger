package io.github.tony8864.chat.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
import io.github.tony8864.chat.group.dto.AddParticipantApiRequest;
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
public class GroupChatAddParticipantControllerTest {
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
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private GroupChatRepository groupChatRepository;
    @Autowired private PasswordHasher passwordHasher;
    @Autowired private TokenService tokenService;

    @Test
    void shouldAddParticipantSuccessfully() throws Exception {
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
        var newUser = User.create(
                UserId.newId(),
                "newUser_" + UUID.randomUUID(),
                Email.of("newUser_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(admin);
        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(newUser);

        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "Team Chat");
        groupChatRepository.save(chat);

        // Generate JWT for admin (requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var addRequest = new AddParticipantApiRequest(
                chat.getChatId().getValue(),
                newUser.getUserId().getValue()
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/add-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chat.getChatId().getValue()))
                .andExpect(jsonPath("$.groupName").value("Team Chat"))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(4)) // now 4
                .andExpect(jsonPath("$.participants[*].userId").value(
                        containsInAnyOrder(
                                admin.getUserId().getValue(),
                                member1.getUserId().getValue(),
                                member2.getUserId().getValue(),
                                newUser.getUserId().getValue()
                        )
                ));
    }

    @Test
    void shouldFailWhenRequesterIsNotAdmin() throws Exception {
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
        var newUser = User.create(
                UserId.newId(),
                "newUser_" + UUID.randomUUID(),
                Email.of("newUser_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(admin);
        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(newUser);

        var participants = new ArrayList<>(List.of(
                Participant.create(admin.getUserId(), Role.ADMIN),
                Participant.create(member1.getUserId(), Role.MEMBER),
                Participant.create(member2.getUserId(), Role.MEMBER)
        ));
        var chat = GroupChat.create(ChatId.newId(), participants, "Team Chat");
        groupChatRepository.save(chat);

        // Generate JWT for member1 (non-admin requester)
        var claims = new UserClaims(
                member1.getUserId().getValue(),
                member1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var addRequest = new AddParticipantApiRequest(
                chat.getChatId().getValue(),
                newUser.getUserId().getValue()
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/add-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
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
        var newUser = User.create(
                UserId.newId(),
                "newUser_" + UUID.randomUUID(),
                Email.of("newUser_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(admin);
        userRepository.save(newUser);

        // Fake chatId (does not exist in DB)
        var fakeChatId = UUID.randomUUID().toString();

        // Generate JWT for admin (valid requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var addRequest = new AddParticipantApiRequest(
                fakeChatId,
                newUser.getUserId().getValue()
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/add-participant")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_CHAT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
}
