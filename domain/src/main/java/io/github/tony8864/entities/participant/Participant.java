package io.github.tony8864.entities.participant;

import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.user.InvalidParticipantException;

public class Participant {
    private final UserId userId;
    private final Role role;

    private Participant(UserId userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public static Participant create(UserId userId, Role role) {
        if (userId == null) throw new InvalidParticipantException("Participant must have a valid userId");
        if (role == null) throw new InvalidParticipantException("Participant must have a valid role");
        return new Participant(userId, role);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public UserId getUserId() {
        return userId;
    }
}
