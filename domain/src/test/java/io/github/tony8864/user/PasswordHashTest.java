package io.github.tony8864.user;

import io.github.tony8864.common.exceptions.EmptyPasswordHashException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {
    @Test
    void newHashShouldCreateInstanceWithValidValue() {
        PasswordHash hash = PasswordHash.newHash("hashedPassword123");
        assertNotNull(hash);
        assertEquals("hashedPassword123", hash.value());
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
}