package io.github.tony8864.user.usecase;

import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.user.Email;
import io.github.tony8864.entities.user.PasswordHash;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.user.usecase.searchuser.SearchUserUseCase;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserRequest;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchUserUseCaseTest {

    private UserRepository userRepository;
    private SearchUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new SearchUserUseCase(userRepository);
    }

    @Test
    void shouldReturnUserWhenUsernameExists() {
        // given
        String username = "alice";
        var user = User.create(
                UserId.newId(),
                username,
                Email.of("alice@example.com"),
                PasswordHash.newHash("hashed-secret")
        );

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        SearchUserResponse response = useCase.search(new SearchUserRequest(username));

        // then
        assertNotNull(response);
        assertEquals(user.getUserId().getValue(), response.userId());
        assertEquals(username, response.username());

        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldThrowWhenUsernameDoesNotExist() {
        // given
        String username = "missingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when / then
        assertThrows(UserNotFoundException.class, () ->
                useCase.search(new SearchUserRequest(username))
        );

        verify(userRepository).findByUsername(username);
    }
}