package io.github.tony8864.user.dto;

public record RegisterUserApiResponse(
        String userId,
        String username,
        String email
) {
}
