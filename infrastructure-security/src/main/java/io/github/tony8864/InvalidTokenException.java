package io.github.tony8864;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
