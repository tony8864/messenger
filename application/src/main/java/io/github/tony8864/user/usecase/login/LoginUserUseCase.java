package io.github.tony8864.user.usecase.login;

import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.entities.user.PresenceStatus;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.user.usecase.login.exception.InvalidCredentialsException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.github.tony8864.user.usecase.login.dto.AuthRequest;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;

import java.time.Duration;
import java.util.Set;

public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public LoginUserUseCase(UserRepository userRepository,
                            PasswordHasher passwordHasher,
                            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public AuthenticatedUser login(AuthRequest request) {
        Email email = Email.of(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email.getValue()));

        if (!user.verifyPassword(request.password(), passwordHasher)) {
            throw new InvalidCredentialsException();
        }

        user.setPresenceStatus(PresenceStatus.ONLINE);
        userRepository.save(user);

        UserClaims claims = new UserClaims(
                user.getUserId().toString(),
                user.getEmail().getValue(),
                Set.of(Role.MEMBER)
        );

        String token = tokenService.generate(claims, Duration.ofHours(1));
        return AuthenticatedUser.from(user, token);
    }
}
