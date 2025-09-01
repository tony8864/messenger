package io.github.tony8864.entities.participant;

import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.user.InvalidParticipantException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {
    private final UserId userId = UserId.of("11111111-1111-1111-1111-111111111111");

    @Test
    void createShouldSucceedWithValidUserIdAndRole() {
        Participant participant = Participant.create(userId, Role.MEMBER);

        assertNotNull(participant);
        assertFalse(participant.isAdmin());
    }

    @Test
    void createShouldThrowWhenUserIdIsNull() {
        assertThrows(InvalidParticipantException.class,
                () -> Participant.create(null, Role.MEMBER));
    }

    @Test
    void createShouldThrowWhenRoleIsNull() {
        assertThrows(InvalidParticipantException.class,
                () -> Participant.create(userId, null));
    }

    @Test
    void isAdminShouldReturnTrueWhenRoleIsAdmin() {
        Participant admin = Participant.create(userId, Role.ADMIN);

        assertTrue(admin.isAdmin());
    }

    @Test
    void isAdminShouldReturnFalseWhenRoleIsNotAdmin() {
        Participant member = Participant.create(userId, Role.MEMBER);

        assertFalse(member.isAdmin());
    }
}