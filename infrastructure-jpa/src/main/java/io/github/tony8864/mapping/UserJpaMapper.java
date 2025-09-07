package io.github.tony8864.mapping;

import io.github.tony8864.entities.user.*;
import io.github.tony8864.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserJpaMapper {
    public User toDomain(UserEntity entity) {
        return User.restore(
                UserId.of(entity.getId().toString()),
                entity.getUsername(),
                Email.of(entity.getEmail()),
                PasswordHash.newHash(entity.getPasswordHash()),
                PresenceStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }

    public UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(UUID.fromString(user.getUserId().getValue())) // convert String -> UUID
                .username(user.getUsername())
                .email(user.getEmail().getValue())
                .passwordHash(user.getPasswordHash().getHash())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
