package io.github.tony8864.entities.user;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean verify(String rawPassword, String hash);
}
