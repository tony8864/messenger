package io.github.tony8864.repositories;

import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(Email email);
    void save(User user);
    void delete(User user);
}
