package io.github.tony8864.chat.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.chat.group.dto.CreateGroupChatApiRequest;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.ChatApplication;
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
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
@Testcontainers
public class GroupChatCreateControllerTest {
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
    void shouldCreateGroupChatSuccessfully() throws Exception {
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

        // Generate token for admin (creator)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        // Build request body (no requesterId)
        var createRequest = new CreateGroupChatApiRequest(
                "My New Group",
                List.of(member1.getUserId().getValue(), member2.getUserId().getValue())
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").isNotEmpty())
                .andExpect(jsonPath("$.groupName").value("My New Group"))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(3)) // admin + 2 members
                .andExpect(jsonPath("$.participants[*].userId").value(
                        containsInAnyOrder(
                                admin.getUserId().getValue(),
                                member1.getUserId().getValue(),
                                member2.getUserId().getValue()
                        )
                ))
                .andExpect(jsonPath("$.participants[?(@.userId=='" + admin.getUserId().getValue() + "')].role")
                        .value(hasItem("ADMIN")))
                .andExpect(jsonPath("$.createdAt").exists());
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
        userRepository.save(member1);

        // Fake requester (not in DB)
        var fakeRequesterId = UUID.randomUUID().toString();

        // Generate token for fake user
        var claims = new UserClaims(fakeRequesterId, "ghost@example.com", Set.of("USER"));
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var createRequest = new CreateGroupChatApiRequest(
                "Ghost Group",
                List.of(member1.getUserId().getValue())
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenParticipantDoesNotExist() throws Exception {
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

        userRepository.save(admin);
        userRepository.save(member1);

        // Fake member (not in DB)
        var fakeMemberId = UUID.randomUUID().toString();

        // Generate token for admin (valid requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var createRequest = new CreateGroupChatApiRequest(
                "Group With Missing Member",
                List.of(member1.getUserId().getValue(), fakeMemberId)
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenGroupNameIsBlank() throws Exception {
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

        userRepository.save(admin);
        userRepository.save(member1);

        // Generate token for admin (valid requester)
        var claims = new UserClaims(
                admin.getUserId().getValue(),
                admin.getEmail().getValue(),
                Set.of("ADMIN")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        var createRequest = new CreateGroupChatApiRequest(
                "",
                List.of(member1.getUserId().getValue())
        );

        // --- Act & Assert ---
        mockMvc.perform(post("/api/chats/group/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GROUP"))
                .andExpect(jsonPath("$.message").exists());
    }
}
