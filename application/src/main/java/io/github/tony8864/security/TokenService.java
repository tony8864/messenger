package io.github.tony8864.security;

import java.time.Duration;

public interface TokenService {
    String generate(UserClaims claims, Duration expiry);
    UserClaims verifyToken(String token);
}
