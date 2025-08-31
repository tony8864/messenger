package io.github.tony8864.repository;

import io.github.tony8864.user.Email;
import io.github.tony8864.user.User;
import io.github.tony8864.user.UserId;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(Email email);
    void save(User user);
    void delete(User user);
}
