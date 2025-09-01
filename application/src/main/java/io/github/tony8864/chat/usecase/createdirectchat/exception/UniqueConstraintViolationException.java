package io.github.tony8864.chat.usecase.createdirectchat.exception;

public class UniqueConstraintViolationException extends RuntimeException {
    public UniqueConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
