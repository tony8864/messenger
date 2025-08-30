package io.github.tony8864.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {
    @Test
    void newIdShouldCreateNonNullValue() {
        assertNotNull(UserId.newId());
    }

    @Test
    void newIdsShouldBeUnique() {
        UserId id1 = UserId.newId();
        UserId id2 = UserId.newId();
        assertNotEquals(id1, id2);
    }

    @Test
    void idsWithSameValueShouldBeEqual() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        UserId id1 = UserId.of(raw);
        UserId id2 = UserId.of(raw);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentValues() {
        UserId id1 = UserId.of("11111111-1111-1111-1111-111111111111");
        UserId id2 = UserId.of("22222222-2222-2222-2222-222222222222");

        assertNotEquals(id1, id2);
    }
}