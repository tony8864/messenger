package io.github.tony8864.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageIdTest {
    @Test
    void newIdShouldCreateNonNullValue() {
        assertNotNull(MessageId.newId());
    }

    @Test
    void newIdsShouldBeUnique() {
        MessageId id1 = MessageId.newId();
        MessageId id2 = MessageId.newId();
        assertNotEquals(id1, id2);
    }

    @Test
    void idsWithSameValueShouldBeEqual() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        MessageId id1 = MessageId.of(raw);
        MessageId id2 = MessageId.of(raw);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentValues() {
        MessageId id1 = MessageId.of("11111111-1111-1111-1111-111111111111");
        MessageId id2 = MessageId.of("22222222-2222-2222-2222-222222222222");

        assertNotEquals(id1, id2);
    }
}