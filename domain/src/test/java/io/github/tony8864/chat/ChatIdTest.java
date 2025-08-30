package io.github.tony8864.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatIdTest {
    @Test
    void newIdShouldCreateNonNullValue() {
        assertNotNull(ChatId.newId());
    }

    @Test
    void newIdsShouldBeUnique() {
        ChatId id1 = ChatId.newId();
        ChatId id2 = ChatId.newId();
        assertNotEquals(id1, id2);
    }

    @Test
    void idsWithSameValueShouldBeEqual() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ChatId id1 = ChatId.of(raw);
        ChatId id2 = ChatId.of(raw);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentValues() {
        ChatId id1 = ChatId.of("11111111-1111-1111-1111-111111111111");
        ChatId id2 = ChatId.of("22222222-2222-2222-2222-222222222222");

        assertNotEquals(id1, id2);
    }
}