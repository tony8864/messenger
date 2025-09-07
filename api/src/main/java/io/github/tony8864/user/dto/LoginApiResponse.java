package io.github.tony8864.user.dto;

public record LoginApiResponse(
        String userId,
        String username,
        String email,
        String presenceStatus,
        String token
) {
}
