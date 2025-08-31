package io.github.tony8864.usecases.user.register;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {
}
