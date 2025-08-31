package io.github.tony8864.user;

import io.github.tony8864.exceptions.EmptyPasswordHashException;
import io.github.tony8864.entities.user.PasswordHash;
import io.github.tony8864.entities.user.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {
    @Test
    void newHashShouldCreateInstanceWithValidValue() {
        PasswordHash hash = PasswordHash.newHash("hashedPassword123");
        assertNotNull(hash);
    }

    @Test
    void newHashShouldThrowExceptionWhenValueIsNull() {
        assertThrows(EmptyPasswordHashException.class, () -> PasswordHash.newHash(null));
    }

    @Test
    void newHashShouldThrowExceptionWhenValueIsBlank() {
        assertThrows(EmptyPasswordHashException.class, () -> PasswordHash.newHash("   "));
    }

    @Test
    void matchesShouldReturnTrueWhenHasherVerifiesCorrectly() {
        PasswordHash stored = PasswordHash.newHash("storedHash");

        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "not-used-in-this-test";
            }

            @Override
            public boolean verify(String rawPassword, String hash) {
                return rawPassword.equals("secret") && hash.equals("storedHash");
            }
        };

        assertTrue(stored.matches("secret", fakeHasher));
    }

    @Test
    void matchesShouldReturnFalseWhenHasherRejects() {
        PasswordHash stored = PasswordHash.newHash("storedHash");

        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "not-used-in-this-test";
            }

            @Override
            public boolean verify(String rawPassword, String hash) {
                return false; // always reject
            }
        };

        assertFalse(stored.matches("wrong", fakeHasher));
    }

    @Test
    void equalHashesShouldBeEqual() {
        PasswordHash h1 = PasswordHash.newHash("abc123");
        PasswordHash h2 = PasswordHash.newHash("abc123");

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
    }

    @Test
    void differentHashesShouldNotBeEqual() {
        PasswordHash h1 = PasswordHash.newHash("abc123");
        PasswordHash h2 = PasswordHash.newHash("xyz456");

        assertNotEquals(h1, h2);
    }

    @Test
    void equalsShouldReturnFalseForNullOrDifferentClass() {
        PasswordHash hash = PasswordHash.newHash("abc123");

        assertNotEquals(null, hash);
        assertNotEquals(new Object(), hash);
    }
}