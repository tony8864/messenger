package io.github.tony8864.user.usecase.logout;

import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.user.PresenceStatus;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class LogoutUseCaseTest {
    private UserRepository userRepository;
    private LogoutUseCase useCase;

    private UserId userId;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new LogoutUseCase(userRepository);

        userId = UserId.of("user-1");
        user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
    }

    @Test
    void logout_shouldSetStatusOfflineAndSave() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        useCase.logout(userId.getValue());

        verify(user).setPresenceStatus(PresenceStatus.OFFLINE);
        verify(userRepository).save(user);
    }

    @Test
    void logout_shouldThrowIfUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.logout(userId.getValue()));

        verify(userRepository, never()).save(any());
    }
}