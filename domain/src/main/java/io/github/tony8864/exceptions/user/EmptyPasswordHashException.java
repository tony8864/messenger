package io.github.tony8864.exceptions.user;

import io.github.tony8864.exceptions.DomainException;

public class EmptyPasswordHashException extends DomainException {
    public EmptyPasswordHashException() {
        super("Password hash cannot be empty");
    }
}
