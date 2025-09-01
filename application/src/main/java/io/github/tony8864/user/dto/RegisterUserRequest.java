package io.github.tony8864.user.dto;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {
}
