package io.github.tony8864.user.dto;

public record RegisterUserApiRequest(
        String username,
        String email,
        String password
) {
}
