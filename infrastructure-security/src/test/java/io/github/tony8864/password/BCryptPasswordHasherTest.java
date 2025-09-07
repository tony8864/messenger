package io.github.tony8864.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void shouldHashAndVerifyPassword() {
        String rawPassword = "secret123";
        String hash = hasher.hash(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash); // hash should not equal raw password
        assertTrue(hasher.verify(rawPassword, hash)); // should match
    }

    @Test
    void shouldNotVerifyWrongPassword() {
        String rawPassword = "secret123";
        String hash = hasher.hash(rawPassword);

        assertFalse(hasher.verify("wrongpass", hash));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "secret123";
        String hash1 = hasher.hash(rawPassword);
        String hash2 = hasher.hash(rawPassword);

        // BCrypt uses salt, so hashes must differ
        assertNotEquals(hash1, hash2);
        assertTrue(hasher.verify(rawPassword, hash1));
        assertTrue(hasher.verify(rawPassword, hash2));
    }
}