package io.github.tony8864.common;

public class UserNotFoundException extends RuntimeException {
    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User with email \"" + email + "\" not found");
    }

    public static UserNotFoundException byId(String id) {
        return new UserNotFoundException("User with id \"" + id + "\" not found");
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("User with id \"" + username + "\" not found");
    }
}
