package io.github.tony8864.exceptions.user;

import io.github.tony8864.exceptions.DomainException;

public class InvalidEmailFormatException extends DomainException {
    public InvalidEmailFormatException() {
        super("Invalid email format");
    }
}
