package io.github.tony8864.chat;

import io.github.tony8864.user.UserId;

import java.util.Objects;
import java.util.UUID;

public class ChatId {
    private final String value;

    private ChatId(String value) {
        this.value = value;
    }

    public static ChatId newId() {
        return new ChatId(UUID.randomUUID().toString());
    }

    public static ChatId of(String value) {
        return new ChatId(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        ChatId chatId = (ChatId) obj;
        return Objects.equals(chatId.value, this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
