package io.github.tony8864.usecases.user.login;

import io.github.tony8864.entities.user.*;
import io.github.tony8864.user.usecase.login.exception.InvalidCredentialsException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.github.tony8864.user.usecase.login.dto.AuthRequest;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import io.github.tony8864.user.usecase.login.LoginUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginUserUseCaseTest {
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private TokenService tokenService;
    private LoginUserUseCase useCase;

    private final Email email = Email.of("test@example.com");
    private final PasswordHash hash = PasswordHash.newHash("hashed-password");
    private final UserId userId = UserId.of("user-1");
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        tokenService = mock(TokenService.class);
        useCase = new LoginUserUseCase(userRepository, passwordHasher, tokenService);

        user = User.create(userId, "alice", email, hash);
    }

    @Test
    void loginShouldSucceedWithValidCredentials() {
        // Arrange
        User testUser = User.create(UserId.newId(),
                "alice",
                Email.of("test@example.com"),
                PasswordHash.newHash("hashedPassword"));  // store "hashedPassword"

        AuthRequest request = new AuthRequest("test@example.com", "secret");

        when(userRepository.findByEmail(Email.of("test@example.com")))
                .thenReturn(Optional.of(testUser));

        when(passwordHasher.verify("secret", "hashedPassword"))
                .thenReturn(true);  // must match the stored hash!

        when(tokenService.generate(any(UserClaims.class), eq(Duration.ofHours(1))))
                .thenReturn("jwt-token");

        // Act
        AuthenticatedUser result = useCase.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.username());
        assertEquals("test@example.com", result.email());
        assertEquals("jwt-token", result.token());

        verify(userRepository).save(testUser);
        assertEquals(PresenceStatus.ONLINE, testUser.getStatus());
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        AuthRequest request = new AuthRequest("notfound@example.com", "secret");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.login(request));

        verify(userRepository, never()).save(any());
        verify(tokenService, never()).generate(any(), any());
    }

    @Test
    void loginShouldThrowWhenPasswordInvalid() {
        AuthRequest request = new AuthRequest("test@example.com", "wrong");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify("wrong", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.login(request));

        verify(userRepository, never()).save(any());
        verify(tokenService, never()).generate(any(), any());
    }
}