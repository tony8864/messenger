package io.github.tony8864.user;

import io.github.tony8864.exceptions.EmptyUsernameException;
import io.github.tony8864.entities.user.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private final UserId userId = UserId.of("11111111-1111-1111-1111-111111111111");
    private final Email email = Email.newEmail("user@example.com");
    private final PasswordHash password = PasswordHash.newHash("hashedPassword123");

    @Test
    void createShouldSucceedWithValidData() {
        User user = User.create(userId, "alice", email, password);

        assertNotNull(user);
        assertEquals(userId, user.getUserId());
        assertEquals("alice", user.getUsername());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void createShouldThrowExceptionWhenUsernameIsNull() {
        assertThrows(EmptyUsernameException.class,
                () -> User.create(userId, null, email, password));
    }

    @Test
    void createShouldThrowExceptionWhenUsernameIsBlank() {
        assertThrows(EmptyUsernameException.class,
                () -> User.create(userId, "   ", email, password));
    }

    @Test
    void changePasswordShouldUpdatePasswordHash() {
        User user = User.create(userId, "bob", email, password);

        PasswordHash newPassword = PasswordHash.newHash("newHashedPassword456");
        user.changePassword(newPassword);

        // verify with a fake hasher that knows only newPassword
        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "not-used";
            }

            @Override
            public boolean verify(String rawPassword, String hash) {
                return rawPassword.equals("new") && hash.equals("newHashedPassword456");
            }
        };

        assertTrue(user.verifyPassword("new", fakeHasher));
        assertFalse(user.verifyPassword("old", fakeHasher));
    }

    @Test
    void verifyPasswordShouldReturnTrueWhenHasherVerifies() {
        User user = User.create(userId, "carol", email, password);

        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "not-used";
            }

            @Override
            public boolean verify(String rawPassword, String hash) {
                return rawPassword.equals("secret") && hash.equals("hashedPassword123");
            }
        };

        assertTrue(user.verifyPassword("secret", fakeHasher));
    }

    @Test
    void verifyPasswordShouldReturnFalseWhenHasherRejects() {
        User user = User.create(userId, "dave", email, password);

        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "not-used";
            }

            @Override
            public boolean verify(String rawPassword, String hash) {
                return false; // always reject
            }
        };

        assertFalse(user.verifyPassword("wrong", fakeHasher));
    }

    @Test
    void createdAtShouldBeCloseToNow() {
        Instant before = Instant.now();
        User user = User.create(userId, "eve", email, password);
        Instant after = Instant.now();

        assertTrue(!user.getCreatedAt().isBefore(before) && !user.getCreatedAt().isAfter(after));
    }

    @Test
    void setPresenceStatusShouldUpdateStatus() {
        User user = User.create(userId, "frank", email, password);

        user.setPresenceStatus(PresenceStatus.ONLINE);

        assertEquals(PresenceStatus.ONLINE, user.getStatus());
    }
}