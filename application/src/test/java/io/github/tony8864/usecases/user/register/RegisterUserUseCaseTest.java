package io.github.tony8864.usecases.user.register;

import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        useCase = new RegisterUserUseCase(userRepository, passwordHasher);
    }

    @Test
    void registerShouldSaveUserWhenEmailNotTaken() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("alice", "alice@example.com", "secret");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordHasher.hash("secret")).thenReturn("hashed-secret");

        // when
        RegisterUserResponse response = useCase.register(request);
        when(passwordHasher.hash("secret")).thenReturn("hashed-secret");
        when(passwordHasher.verify("secret", "hashed-secret")).thenReturn(true);

        // then
        assertNotNull(response);
        assertEquals("alice", response.username());
        assertEquals("alice@example.com", response.email());

        // verify repo.save() was called with a User containing the hashed password
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals("alice", saved.getUsername());
        assertEquals("alice@example.com", saved.getEmail().getValue());
        assertTrue(saved.verifyPassword("secret", passwordHasher));
    }
}