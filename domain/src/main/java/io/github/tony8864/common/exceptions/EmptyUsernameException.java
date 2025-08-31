package io.github.tony8864.common.exceptions;

public class EmptyUsernameException extends DomainException {
    public EmptyUsernameException() {
        super("Username cannot be empty");
    }
}
