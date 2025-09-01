package io.github.tony8864.usecases.user.login;

public record AuthenticatedUser(String userId, String username, String token) {
}
