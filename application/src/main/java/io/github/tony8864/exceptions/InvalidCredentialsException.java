package io.github.tony8864.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("The password is not correct");
    }
}
