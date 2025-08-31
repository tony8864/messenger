package io.github.tony8864.entities.user;

import io.github.tony8864.exceptions.EmptyPasswordHashException;

import java.util.Objects;

public class PasswordHash {
    private final String hash;

    private PasswordHash(String hash) {
        this.hash = hash;
    }

    public static PasswordHash newHash(String hash) {
        if (hash == null || hash.isBlank()) throw new EmptyPasswordHashException();
        return new PasswordHash(hash);
    }

    public boolean matches(String rawPassword, PasswordHasher hasher) {
        return hasher.verify(rawPassword, this.hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PasswordHash that = (PasswordHash) o;
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
