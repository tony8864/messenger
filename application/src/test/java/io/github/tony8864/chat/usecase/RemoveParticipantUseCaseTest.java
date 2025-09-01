package io.github.tony8864.chat.usecase;

import io.github.tony8864.chat.usecase.removeparticipant.dto.RemoveParticipantRequest;
import io.github.tony8864.chat.usecase.removeparticipant.dto.RemoveParticipantResponse;
import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.removeparticipant.RemoveParticipantUseCase;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.chat.UserNotInChatException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveParticipantUseCaseTest {
    private UserRepository userRepository;
    private GroupChatRepository groupChatRepository;
    private RemoveParticipantUseCase useCase;

    private UserId requesterId;
    private UserId targetId;
    private ChatId chatId;
    private User requester;
    private User target;
    private GroupChat chat;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        useCase = new RemoveParticipantUseCase(userRepository, groupChatRepository);

        requesterId = UserId.of("admin-1");
        targetId = UserId.of("user-2");
        chatId = ChatId.of("chat-123");

        requester = mock(User.class);
        when(requester.getUserId()).thenReturn(requesterId);

        target = mock(User.class);
        when(target.getUserId()).thenReturn(targetId);

        Participant admin = Participant.create(requesterId, Role.ADMIN);
        Participant member1 = Participant.create(targetId, Role.MEMBER);
        Participant member2 = Participant.create(UserId.of("user-3"), Role.MEMBER);

        chat = GroupChat.create(chatId, new ArrayList<>(List.of(admin, member1, member2)), "Test Group");
    }

    @Test
    void removeParticipant_success() {
        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), targetId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));

        RemoveParticipantResponse response = useCase.remove(request);

        assertEquals(chatId.getValue(), response.chatId());
        assertFalse(response.participantDtos().stream()
                        .anyMatch(dto -> dto.userId().equals(targetId.getValue())),
                "Removed user should not appear in the response participants list");

        verify(groupChatRepository).save(chat);
    }

    @Test
    void removeParticipant_groupNotFound() {
        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), targetId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(GroupChatNotFoundException.class, () -> useCase.remove(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void removeParticipant_requesterNotFound() {
        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), targetId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.remove(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void removeParticipant_targetNotFound() {
        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), targetId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.remove(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void removeParticipant_requesterNotAdmin() {
        // Rebuild chat: requester is only a member
        Participant admin = Participant.create(UserId.of("another-admin"), Role.ADMIN);
        Participant requesterAsMember = Participant.create(requesterId, Role.MEMBER);
        Participant member2 = Participant.create(targetId, Role.MEMBER);

        chat = GroupChat.create(chatId,
                new ArrayList<>(List.of(admin, requesterAsMember, member2)),
                "Test Group");

        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), targetId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));

        assertThrows(UnauthorizedOperationException.class, () -> useCase.remove(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void removeParticipant_userNotInChat() {
        UserId notInChatId = UserId.of("ghost-1");
        RemoveParticipantRequest request =
                new RemoveParticipantRequest(chatId.getValue(), requesterId.getValue(), notInChatId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(notInChatId)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(UserNotInChatException.class, () -> useCase.remove(request));
        verify(groupChatRepository, never()).save(any());
    }
}