package io.github.tony8864.entities.message;

import java.util.Objects;
import java.util.UUID;

public class MessageId {
    private final String value;

    private MessageId(String value) {
        this.value = value;
    }

    public static MessageId newId() {
        return new MessageId(UUID.randomUUID().toString());
    }

    public static MessageId of(String value) {
        return new MessageId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        MessageId messageId = (MessageId) obj;
        return Objects.equals(messageId.value, this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
