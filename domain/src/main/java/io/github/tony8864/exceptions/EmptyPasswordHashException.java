package io.github.tony8864.exceptions;

public class EmptyPasswordHashException extends DomainException {
    public EmptyPasswordHashException() {
        super("Password hash cannot be empty");
    }
}
