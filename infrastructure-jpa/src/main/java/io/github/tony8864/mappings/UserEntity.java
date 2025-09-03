package io.github.tony8864.mappings;

import io.github.tony8864.entities.user.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(UUID.fromString(user.getUserId().getValue())) // convert String -> UUID
                .username(user.getUsername())
                .email(user.getEmail().getValue())
                .passwordHash(user.getPasswordHash().getHash())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toDomain() {
        return User.restore(
                UserId.of(id.toString()),
                username,
                Email.of(email),
                PasswordHash.newHash(passwordHash),
                PresenceStatus.valueOf(status),
                createdAt
        );
    }
}
