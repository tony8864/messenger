package io.github.tony8864.exceptions.chat;

import io.github.tony8864.exceptions.DomainException;

public class InvalidGroupException extends DomainException {
    public InvalidGroupException(String message) {
        super(message);
    }
}
