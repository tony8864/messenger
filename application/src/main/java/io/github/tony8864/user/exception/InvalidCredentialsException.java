package io.github.tony8864.user.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("The password is not correct");
    }
}
