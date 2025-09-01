package io.github.tony8864.exceptions.message;

import io.github.tony8864.exceptions.DomainException;

public class EmptyMessageContentException extends DomainException {
    public EmptyMessageContentException() {
        super("Message content cannot be empty");
    }
}
