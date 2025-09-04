package io.github.tony8864.adapter;

import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PasswordHash;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class JpaGroupChatRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    private UserId user1Id;
    private UserId user2Id;
    private UserId user3Id;

    @BeforeEach
    void setupUsers() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        user1Id = UserId.of(UUID.randomUUID().toString());
        user2Id = UserId.of(UUID.randomUUID().toString());
        user3Id = UserId.of(UUID.randomUUID().toString());

        User user1 = User.create(
                user1Id,
                "alice_" + suffix,
                Email.of("alice_" + suffix + "@example.com"),
                PasswordHash.newHash("hash1")
        );
        User user2 = User.create(
                user2Id,
                "bob_" + suffix,
                Email.of("bob_" + suffix + "@example.com"),
                PasswordHash.newHash("hash2")
        );
        User user3 = User.create(
                user3Id,
                "carol_" + suffix,
                Email.of("carol_" + suffix + "@example.com"),
                PasswordHash.newHash("hash3")
        );

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }

    @Test
    void saveAndFindById_shouldPersistAndLoadGroupChat() {
        // given
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        GroupChat chat = GroupChat.create(
                chatId,
                List.of(
                        Participant.create(user1Id, Role.ADMIN),
                        Participant.create(user2Id, Role.MEMBER),
                        Participant.create(user3Id, Role.MEMBER)
                ),
                "dev-team"
        );

        // when
        groupChatRepository.save(chat);
        Optional<GroupChat> found = groupChatRepository.findById(chatId);

        // then
        assertTrue(found.isPresent());
        GroupChat loaded = found.get();
        assertEquals(chatId, loaded.getChatId());
        assertEquals("dev-team", loaded.getGroupName());
        assertEquals(3, loaded.getParticipants().size());
        assertTrue(loaded.getParticipants().stream().anyMatch(p -> p.getRole() == Role.ADMIN));
    }

    @Test
    void delete_shouldRemoveGroupChat() {
        // given
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        GroupChat chat = GroupChat.create(
                chatId,
                List.of(
                        Participant.create(user1Id, Role.ADMIN),
                        Participant.create(user2Id, Role.MEMBER),
                        Participant.create(user3Id, Role.MEMBER)
                ),
                "qa-team"
        );
        groupChatRepository.save(chat);

        // when
        groupChatRepository.delete(chat);
        Optional<GroupChat> found = groupChatRepository.findById(chatId);

        // then
        assertTrue(found.isEmpty());
    }
}