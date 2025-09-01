package io.github.tony8864.security;

import io.github.tony8864.usecases.user.login.AuthenticatedUser;

import java.time.Duration;

public interface TokenService {
    String generate(UserClaims claims, Duration expiry);
    UserClaims verifyToken(String token);
}
