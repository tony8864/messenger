package io.github.tony8864;

import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
public class JwtTokenService implements TokenService {

    private final SecretKey signingKey;

    public JwtTokenService(@Value("${jwt.secret}") String secret) {
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generate(UserClaims claims, Duration expiry) {
        Instant now = Instant.now();
        Instant expiryInstant = now.plus(expiry);

        return Jwts.builder()
                .subject(claims.userId())
                .claim("email", claims.email())
                .claim("roles", claims.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiry)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public UserClaims verifyToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            List<String> rolesList = claims.get("roles", List.class);
            HashSet<String> roles = new HashSet<>(rolesList);

            return new UserClaims(userId, email, roles);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired JWT", e);
        }
    }
}
