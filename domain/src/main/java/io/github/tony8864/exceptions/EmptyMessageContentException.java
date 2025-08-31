package io.github.tony8864.exceptions;

public class EmptyMessageContentException extends DomainException {
    public EmptyMessageContentException() {
        super("Message content cannot be empty");
    }
}
