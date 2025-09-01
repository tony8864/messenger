package io.github.tony8864.chat.usecase.createdirectchat;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatRequest;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatResponse;
import io.github.tony8864.chat.usecase.createdirectchat.exception.InvalidChatException;
import io.github.tony8864.chat.usecase.createdirectchat.exception.UniqueConstraintViolationException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CreateDirectChatUseCaseTest {
    private UserRepository userRepository;
    private DirectChatRepository directChatRepository;
    private CreateDirectChatUseCase useCase;

    private UserId requesterId;
    private UserId otherId;
    private User requester;
    private User other;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        directChatRepository = mock(DirectChatRepository.class);
        useCase = new CreateDirectChatUseCase(userRepository, directChatRepository);

        requesterId = UserId.of("a-user");
        otherId = UserId.of("b-user");

        requester = mock(User.class);
        when(requester.getUserId()).thenReturn(requesterId);

        other = mock(User.class);
        when(other.getUserId()).thenReturn(otherId);
    }

    @Test
    void createDirectChat_success() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), otherId.getValue());

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(otherId)).thenReturn(Optional.of(other));
        when(directChatRepository.findByUsers(requesterId, otherId)).thenReturn(Optional.empty());

        CreateDirectChatResponse response = useCase.create(request);

        assertEquals(2, response.participantIds().size());
        assertTrue(response.participantIds().containsAll(
                List.of(requesterId.getValue(), otherId.getValue())));

        verify(directChatRepository).save(any(DirectChat.class));
    }

    @Test
    void createDirectChat_shouldReturnExistingIfPresent() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), otherId.getValue());

        DirectChat existing = DirectChat.create(ChatId.newId(), List.of(requesterId, otherId));

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(otherId)).thenReturn(Optional.of(other));
        when(directChatRepository.findByUsers(requesterId, otherId)).thenReturn(Optional.of(existing));

        CreateDirectChatResponse response = useCase.create(request);

        assertEquals(existing.getChatId().getValue(), response.chatId());
        assertEquals(
                List.of(requesterId.getValue(), otherId.getValue()),
                response.participantIds()
        );
        verify(directChatRepository, never()).save(any());
    }

    @Test
    void createDirectChat_shouldThrowIfSameUser() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), requesterId.getValue());

        assertThrows(InvalidChatException.class, () -> useCase.create(request));
        verifyNoInteractions(userRepository, directChatRepository);
    }


    @Test
    void createDirectChat_requesterNotFound() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), otherId.getValue());

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.create(request));
        verify(directChatRepository, never()).save(any());
    }

    @Test
    void createDirectChat_otherUserNotFound() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), otherId.getValue());

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(otherId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.create(request));
        verify(directChatRepository, never()).save(any());
    }

    @Test
    void createDirectChat_shouldHandleUniqueConstraintViolation() {
        CreateDirectChatRequest request =
                new CreateDirectChatRequest(requesterId.getValue(), otherId.getValue());

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(otherId)).thenReturn(Optional.of(other));
        when(directChatRepository.findByUsers(requesterId, otherId)).thenReturn(Optional.empty());

        doThrow(new UniqueConstraintViolationException("Duplicate", null))
                .when(directChatRepository).save(any(DirectChat.class));

        DirectChat winner = DirectChat.create(ChatId.newId(), List.of(requesterId, otherId));
        when(directChatRepository.findByUsers(requesterId, otherId)).thenReturn(Optional.of(winner));

        CreateDirectChatResponse response = useCase.create(request);

        assertEquals(winner.getChatId().getValue(), response.chatId());
        assertEquals(
                List.of(requesterId.getValue(), otherId.getValue()),
                response.participantIds()
        );

        verify(directChatRepository, atLeastOnce()).findByUsers(requesterId, otherId);
    }
}