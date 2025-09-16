package io.github.tony8864.user.repository;

import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(Email email);
    Optional<User> findByUsername(String username);
    void save(User user);
    void delete(User user);
}
