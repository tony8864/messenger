package io.github.tony8864.exceptions.user;

import io.github.tony8864.exceptions.DomainException;

public class EmptyUsernameException extends DomainException {
    public EmptyUsernameException() {
        super("Username cannot be empty");
    }
}
