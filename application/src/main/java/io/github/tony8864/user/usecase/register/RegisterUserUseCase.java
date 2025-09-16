package io.github.tony8864.user.usecase.register;

import io.github.tony8864.entities.user.*;
import io.github.tony8864.user.usecase.register.exception.UserAlreadyExistsException;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.user.usecase.register.dto.RegisterUserRequest;
import io.github.tony8864.user.usecase.register.dto.RegisterUserResponse;
import io.github.tony8864.user.usecase.register.exception.UsernameAlreadyExistsException;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        Email email = Email.of(request.email());
        String username = request.username();

        userRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException(email.getValue());
                });

        userRepository.findByUsername(username)
                .ifPresent(u -> {
                    throw new UsernameAlreadyExistsException(username);
                });

        String hashed = passwordHasher.hash(request.password());
        PasswordHash hash = PasswordHash.newHash(hashed);

        User user = User.create(UserId.newId(), request.username(), email, hash);
        userRepository.save(user);

        return RegisterUserResponse.from(user);
    }
}
