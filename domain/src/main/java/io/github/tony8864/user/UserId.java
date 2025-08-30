package io.github.tony8864.user;

import java.util.Objects;
import java.util.UUID;

public class UserId {

    private final String value;

    private UserId(String value) {
        this.value = value;
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    static UserId of(String value) {
        return new UserId(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        UserId userId = (UserId) obj;
        return Objects.equals(userId.value, this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
