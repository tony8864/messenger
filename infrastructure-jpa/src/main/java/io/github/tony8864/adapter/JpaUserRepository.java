package io.github.tony8864.adapter;

import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.UserEntity;
import io.github.tony8864.mapping.UserMapper;
import io.github.tony8864.repository.SpringDataUserRepository;
import io.github.tony8864.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UserId userId) {
        return springDataUserRepository.findById(UUID.fromString(userId.getValue()))
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.getValue())
                .map(userMapper::toDomain);
    }

    @Override
    public void save(User user) {
        springDataUserRepository.save(userMapper.fromDomain(user));
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.delete(userMapper.fromDomain(user));
    }
}
