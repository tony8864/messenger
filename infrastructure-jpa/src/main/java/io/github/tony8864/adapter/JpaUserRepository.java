package io.github.tony8864.adapter;

import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.mappings.UserEntity;
import io.github.tony8864.repositories.SpringDataUserRepository;
import io.github.tony8864.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findById(UserId userId) {
        return springDataUserRepository.findById(UUID.fromString(userId.getValue()))
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.getValue())
                .map(UserEntity::toDomain);
    }

    @Override
    public void save(User user) {
        springDataUserRepository.save(UserEntity.fromDomain(user));
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.delete(UserEntity.fromDomain(user));
    }
}
