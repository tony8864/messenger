package io.github.tony8864.user;

public interface PasswordHasher {
    boolean verify(String rawPassword, String hash);
}
