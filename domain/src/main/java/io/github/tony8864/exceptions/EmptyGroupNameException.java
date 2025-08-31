package io.github.tony8864.exceptions;

public class EmptyGroupNameException extends DomainException {
    public EmptyGroupNameException() {
        super("GroupChat cannot have empty group name");
    }
}
