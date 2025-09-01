package io.github.tony8864.usecases.user.login;

import io.github.tony8864.entities.user.User;

public record AuthenticatedUser(
        String userId,
        String username,
        String email,
        String presenceStatus,
        String token
) {
    public static AuthenticatedUser from(User user, String token) {
        return new AuthenticatedUser(
                user.getUserId().toString(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getStatus().name(),
                token
        );
    }
}
