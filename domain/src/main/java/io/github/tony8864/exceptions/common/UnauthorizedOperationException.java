package io.github.tony8864.exceptions.common;

import io.github.tony8864.exceptions.DomainException;

public class UnauthorizedOperationException extends DomainException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
