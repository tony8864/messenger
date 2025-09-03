package io.github.tony8864.entities.user;

import io.github.tony8864.exceptions.user.EmptyUsernameException;

import java.time.Instant;

public class User {
    private final Instant createdAt;
    private final String username;
    private final UserId userId;
    private final Email email;

    private PasswordHash passwordHash;
    private PresenceStatus status;

    private User(UserId userId,
                 String username,
                 Email email,
                 PasswordHash passwordHash) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = PresenceStatus.OFFLINE;
        this.createdAt = Instant.now();
    }

    private User(UserId userId,
                 String username,
                 Email email,
                 PasswordHash passwordHash,
                 PresenceStatus status,
                 Instant createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static User create(UserId userId, String username, Email email, PasswordHash passwordHash) {
        if (username == null || username.isBlank()) throw new EmptyUsernameException();
        return new User(userId, username, email, passwordHash);
    }

    public static User restore(UserId id, String username, Email email, PasswordHash passwordHash, PresenceStatus status, Instant createdAt) {
        return new User(id, username, email, passwordHash, status, createdAt);
    }

    public boolean verifyPassword(String rawPassword, PasswordHasher hasher) {
        return passwordHash.matches(rawPassword, hasher);
    }

    // * --- Setters --- * //
    public void setPresenceStatus(PresenceStatus presenceStatus) {
        this.status = presenceStatus;
    }

    // * --- Getters --- * //
    public void changePassword(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserId getUserId() {
        return userId;
    }

    public Email getEmail() { return email;}

    public String getUsername() { return username;}

    public Instant getCreatedAt() {
        return createdAt;
    }

    public PasswordHash getPasswordHash() { return passwordHash; }

    public PresenceStatus getStatus() { return status; }
}
