package io.github.tony8864.adapter;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class JpaDirectChatRepositoryTest {
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
    private DirectChatRepository directChatRepository;

    private UserId user1Id;
    private UserId user2Id;

    @BeforeEach
    void setupUsers() {
        user1Id = UserId.of(UUID.randomUUID().toString());
        user2Id = UserId.of(UUID.randomUUID().toString());

        // unique values for each test
        String username1 = "alice_" + UUID.randomUUID();
        String username2 = "bob_" + UUID.randomUUID();
        String email1 = "alice_" + UUID.randomUUID() + "@example.com";
        String email2 = "bob_" + UUID.randomUUID() + "@example.com";

        User user1 = User.create(user1Id, username1, Email.of(email1), PasswordHash.newHash("hash1"));
        User user2 = User.create(user2Id, username2, Email.of(email2), PasswordHash.newHash("hash2"));

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    void saveAndFindById() {
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        DirectChat chat = DirectChat.create(chatId, List.of(user1Id, user2Id));

        directChatRepository.save(chat);

        Optional<DirectChat> found = directChatRepository.findById(chatId);

        assertTrue(found.isPresent());
        assertEquals(chatId, found.get().getChatId());
        assertTrue(found.get().getParticipants().containsAll(List.of(user1Id, user2Id)));
    }

    @Test
    void findByUsersShouldReturnChat() {
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        DirectChat chat = DirectChat.create(chatId, List.of(user1Id, user2Id));
        directChatRepository.save(chat);

        Optional<DirectChat> found = directChatRepository.findByUsers(user1Id, user2Id);

        assertTrue(found.isPresent());
        assertEquals(chatId, found.get().getChatId());
    }

    @Test
    void findByUsersShouldReturnEmptyWhenNotExists() {
        Optional<DirectChat> found = directChatRepository.findByUsers(user1Id, user2Id);

        assertTrue(found.isEmpty());
    }

    @Test
    void deleteShouldRemoveChat() {
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        DirectChat chat = DirectChat.create(chatId, List.of(user1Id, user2Id));
        directChatRepository.save(chat);

        directChatRepository.delete(chat);

        Optional<DirectChat> found = directChatRepository.findById(chatId);
        assertTrue(found.isEmpty());
    }
}