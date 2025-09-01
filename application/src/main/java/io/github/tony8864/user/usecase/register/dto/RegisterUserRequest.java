package io.github.tony8864.user.usecase.register.dto;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {
}
