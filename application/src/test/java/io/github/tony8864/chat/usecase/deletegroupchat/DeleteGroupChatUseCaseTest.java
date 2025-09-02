package io.github.tony8864.chat.usecase.deletegroupchat;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.deletegroupchat.dto.DeleteGroupChatRequest;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeleteGroupChatUseCaseTest {
    private UserRepository userRepository;
    private GroupChatRepository groupChatRepository;
    private DeleteGroupChatUseCase useCase;

    private UserId adminId;
    private UserId memberId;
    private ChatId chatId;
    private User admin;
    private User member;
    private GroupChat chat;
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        useCase = new DeleteGroupChatUseCase(userRepository, groupChatRepository);

        adminId = UserId.of("admin-1");
        memberId = UserId.of("member-1");
        chatId = ChatId.of("chat-1");

        admin = mock(User.class);
        when(admin.getUserId()).thenReturn(adminId);

        member = mock(User.class);
        when(member.getUserId()).thenReturn(memberId);

        Participant adminP = Participant.create(adminId, Role.ADMIN);
        Participant memberP = Participant.create(memberId, Role.MEMBER);
        chat = GroupChat.create(chatId, new ArrayList<>(List.of(adminP, memberP, Participant.create(UserId.of("m2"), Role.MEMBER))), "Test Group");
    }

    @Test
    void deleteGroupChat_success() {
        DeleteGroupChatRequest request = new DeleteGroupChatRequest(chatId.getValue(), adminId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        assertDoesNotThrow(() -> useCase.delete(request));

        verify(groupChatRepository).delete(chat);
    }

    @Test
    void deleteGroupChat_shouldThrowIfGroupNotFound() {
        DeleteGroupChatRequest request = new DeleteGroupChatRequest(chatId.getValue(), adminId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(GroupChatNotFoundException.class, () -> useCase.delete(request));
        verify(groupChatRepository, never()).delete(any());
    }

    @Test
    void deleteGroupChat_shouldThrowIfRequesterNotFound() {
        DeleteGroupChatRequest request = new DeleteGroupChatRequest(chatId.getValue(), adminId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.delete(request));
        verify(groupChatRepository, never()).delete(any());
    }

    @Test
    void deleteGroupChat_shouldThrowIfRequesterNotAdmin() {
        DeleteGroupChatRequest request = new DeleteGroupChatRequest(chatId.getValue(), memberId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(UnauthorizedOperationException.class, () -> useCase.delete(request));
        verify(groupChatRepository, never()).delete(any());
    }
}