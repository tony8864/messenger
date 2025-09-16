package io.github.tony8864.adapter;

import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PasswordHash;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class JpaUserRepositoryTest {

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

    @Test
    void saveAndFindById() {
        UserId id = UserId.of(UUID.randomUUID().toString());
        User user = User.create(id, "testuser", Email.of("test@example.com"), PasswordHash.newHash("hash123"));

        userRepository.save(user);

        Optional<User> found = userRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail().getValue());
    }

    @Test
    void findByEmailShouldReturnUser() {
        UserId id = UserId.of(UUID.randomUUID().toString());
        User user = User.create(id, "byEmail", Email.of("email@example.com"), PasswordHash.newHash("hash123"));
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail(Email.of("email@example.com"));

        assertTrue(found.isPresent());
        assertEquals("byEmail", found.get().getUsername());
    }

    @Test
    void findByEmailShouldReturnEmptyWhenNotExists() {
        Optional<User> found = userRepository.findByEmail(Email.of("missing@example.com"));

        assertTrue(found.isEmpty());
    }

    @Test
    void deleteShouldRemoveUser() {
        UserId id = UserId.of(UUID.randomUUID().toString());
        User user = User.create(id, "todelete", Email.of("delete@example.com"), PasswordHash.newHash("hash123"));
        userRepository.save(user);

        userRepository.delete(user);

        Optional<User> found = userRepository.findById(id);
        assertTrue(found.isEmpty());
    }

    @Test
    void saveShouldFailOnDuplicateEmail() {
        User user1 = User.create(UserId.of(UUID.randomUUID().toString()), "dup1", Email.of("dup@example.com"), PasswordHash.newHash("hash123"));
        User user2 = User.create(UserId.of(UUID.randomUUID().toString()), "dup2", Email.of("dup@example.com"), PasswordHash.newHash("hash123"));

        userRepository.save(user1);

        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    @Test
    void findByUsernameShouldReturnUser() {
        UserId id = UserId.of(UUID.randomUUID().toString());
        User user = User.create(id, "findme", Email.of("findme@example.com"), PasswordHash.newHash("hash123"));
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("findme");

        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getUsername());
        assertEquals("findme@example.com", found.get().getEmail().getValue());
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenNotExists() {
        Optional<User> found = userRepository.findByUsername("missinguser");

        assertTrue(found.isEmpty());
    }
}