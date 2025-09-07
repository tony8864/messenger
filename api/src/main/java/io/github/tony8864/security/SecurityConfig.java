package io.github.tony8864.security;

import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.jwt.JwtTokenService;
import io.github.tony8864.password.BCryptPasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public TokenService tokenService(@Value("${jwt.secret}") String secret) {
        return new JwtTokenService(secret);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }
}
