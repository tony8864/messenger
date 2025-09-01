package io.github.tony8864.usecases.user.register.dto;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {
}
