package io.github.tony8864.mapping;

import io.github.tony8864.entities.user.*;
import io.github.tony8864.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final UserJpaMapper mapper = new UserJpaMapper();

    @Test
    void toDomain_shouldMapEntityToDomain() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "alice";
        String email = "alice@example.com";
        String passwordHash = "hashed_pw";
        String status = "ONLINE";
        Instant createdAt = Instant.now();

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .status(status)
                .createdAt(createdAt)
                .build();

        // when
        User domain = mapper.toDomain(entity);

        // then
        assertEquals(userId.toString(), domain.getUserId().getValue());
        assertEquals(username, domain.getUsername());
        assertEquals(email, domain.getEmail().getValue());
        assertEquals(passwordHash, domain.getPasswordHash().getHash());
        assertEquals(PresenceStatus.ONLINE, domain.getStatus());
        assertEquals(createdAt, domain.getCreatedAt());
    }

    @Test
    void fromDomain_shouldMapDomainToEntity() {
        // given
        UserId userId = UserId.of(UUID.randomUUID().toString());
        String username = "bob";
        Email email = Email.of("bob@example.com");
        PasswordHash passwordHash = PasswordHash.newHash("secret123");
        PresenceStatus status = PresenceStatus.OFFLINE;
        Instant createdAt = Instant.now();

        User domain = User.restore(
                userId,
                username,
                email,
                passwordHash,
                status,
                createdAt
        );

        // when
        UserEntity entity = mapper.fromDomain(domain);

        // then
        assertEquals(UUID.fromString(userId.getValue()), entity.getId());
        assertEquals(username, entity.getUsername());
        assertEquals(email.getValue(), entity.getEmail());
        assertEquals(passwordHash.getHash(), entity.getPasswordHash());
        assertEquals(status.name(), entity.getStatus());
        assertEquals(createdAt, entity.getCreatedAt());
    }
}