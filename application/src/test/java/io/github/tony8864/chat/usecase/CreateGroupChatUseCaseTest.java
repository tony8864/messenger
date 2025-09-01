package io.github.tony8864.chat.usecase;

import io.github.tony8864.chat.usecase.creategroupchat.dto.CreateGroupChatRequest;
import io.github.tony8864.chat.usecase.creategroupchat.dto.CreateGroupChatResponse;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.creategroupchat.CreateGroupChatUseCase;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.chat.InvalidGroupException;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateGroupChatUseCaseTest {
    private UserRepository userRepository;
    private GroupChatRepository groupChatRepository;
    private CreateGroupChatUseCase useCase;

    private UserId requesterId;
    private User requester;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        useCase = new CreateGroupChatUseCase(userRepository, groupChatRepository);

        requesterId = UserId.of("req-1");
        requester = mock(User.class);
        when(requester.getUserId()).thenReturn(requesterId);

        user1 = mock(User.class);
        when(user1.getUserId()).thenReturn(UserId.of("user-1"));

        user2 = mock(User.class);
        when(user2.getUserId()).thenReturn(UserId.of("user-2"));
    }

    @Test
    void createGroupChat_success() {
        List<String> userIds = List.of("req-1", "user-1", "user-2");
        CreateGroupChatRequest request =
                new CreateGroupChatRequest("req-1", "Test Group", userIds);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.of(user1));
        when(userRepository.findById(UserId.of("user-2"))).thenReturn(Optional.of(user2));

        CreateGroupChatResponse response = useCase.create(request);

        assertEquals("Test Group", response.groupName());
        assertTrue(response.participantDtos().stream()
                        .anyMatch(p -> p.userId().equals("req-1") && p.role() == Role.ADMIN),
                "Requester should be an ADMIN");
        assertTrue(response.participantDtos().stream()
                .anyMatch(p -> p.userId().equals("user-1") && p.role() == Role.MEMBER));
        assertTrue(response.participantDtos().stream()
                .anyMatch(p -> p.userId().equals("user-2") && p.role() == Role.MEMBER));

        verify(groupChatRepository).save(any());
    }

    @Test
    void createGroupChat_requesterNotFound() {
        List<String> userIds = List.of("req-1", "user-1");
        CreateGroupChatRequest request =
                new CreateGroupChatRequest("req-1", "Group", userIds);

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.create(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void createGroupChat_memberNotFound() {
        List<String> userIds = List.of("req-1", "user-1");
        CreateGroupChatRequest request =
                new CreateGroupChatRequest("req-1", "Group", userIds);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.create(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void createGroupChat_requesterShouldNotBeDuplicated() {
        List<String> userIds = List.of("req-1", "user-1", "req-1", "user-2");
        CreateGroupChatRequest request =
                new CreateGroupChatRequest("req-1", "Group", userIds);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.of(user1));
        when(userRepository.findById(UserId.of("user-2"))).thenReturn(Optional.of(user2));

        CreateGroupChatResponse response = useCase.create(request);

        long requesterCount = response.participantDtos().stream()
                .filter(p -> p.userId().equals("req-1"))
                .count();

        assertEquals(1, requesterCount, "Requester should appear only once as ADMIN");
        verify(groupChatRepository).save(any());
    }

    @Test
    void createGroupChat_shouldFailIfLessThan3UniqueParticipants() {
        List<String> userIds = List.of("req-1", "user-1", "req-1");
        CreateGroupChatRequest request =
                new CreateGroupChatRequest("req-1", "Group", userIds);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.of(user1));

        assertThrows(InvalidGroupException.class, () -> useCase.create(request));
        verify(groupChatRepository, never()).save(any());
    }
}