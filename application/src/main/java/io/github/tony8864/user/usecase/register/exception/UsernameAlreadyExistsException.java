package io.github.tony8864.user.usecase.register.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("User with username: " + username + " already exists");
    }
}
