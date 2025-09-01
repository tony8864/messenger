package io.github.tony8864.exceptions.chat;

import io.github.tony8864.exceptions.DomainException;

public class UserAlreadyParticipantException extends DomainException {
    public UserAlreadyParticipantException() {
        super("User is already a participant in the chat");
    }
}
