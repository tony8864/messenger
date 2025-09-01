package io.github.tony8864.exceptions.user;

import io.github.tony8864.exceptions.DomainException;

public class InvalidParticipantException extends DomainException {
    public InvalidParticipantException(String message) {
        super(message);
    }
}
