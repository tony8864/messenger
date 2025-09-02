package io.github.tony8864.chat.usecase.renamegroupchat;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatRequest;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatResponse;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RenameGroupChatUseCaseTest {
    private UserRepository userRepository;
    private GroupChatRepository groupChatRepository;
    private RenameGroupChatUseCase useCase;

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
        useCase = new RenameGroupChatUseCase(userRepository, groupChatRepository);

        adminId = UserId.of("admin-1");
        memberId = UserId.of("member-1");
        chatId = ChatId.of("chat-1");

        admin = mock(User.class);
        when(admin.getUserId()).thenReturn(adminId);

        member = mock(User.class);
        when(member.getUserId()).thenReturn(memberId);

        Participant adminP = Participant.create(adminId, Role.ADMIN);
        Participant memberP = Participant.create(memberId, Role.MEMBER);
        chat = GroupChat.create(chatId,
                new ArrayList<>(List.of(adminP, memberP, Participant.create(UserId.of("m2"), Role.MEMBER))),
                "Old Group");
    }

    @Test
    void renameGroupChat_success() {
        RenameGroupChatRequest request =
                new RenameGroupChatRequest(chatId.getValue(), adminId.getValue(), "New Group");

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        RenameGroupChatResponse response = useCase.rename(request);

        assertEquals(chatId.getValue(), response.chatId());
        assertEquals("New Group", response.groupName());
        assertTrue(response.participantDtos().stream()
                .anyMatch(dto -> dto.userId().equals(adminId.getValue()) && dto.role() == Role.ADMIN));

        verify(groupChatRepository).save(chat);
    }

    @Test
    void renameGroupChat_shouldThrowIfRequesterNotFound() {
        RenameGroupChatRequest request =
                new RenameGroupChatRequest(chatId.getValue(), adminId.getValue(), "New Group");

        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.rename(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void renameGroupChat_shouldThrowIfGroupNotFound() {
        RenameGroupChatRequest request =
                new RenameGroupChatRequest(chatId.getValue(), adminId.getValue(), "New Group");

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(GroupChatNotFoundException.class, () -> useCase.rename(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void renameGroupChat_shouldThrowIfRequesterNotAdmin() {
        RenameGroupChatRequest request =
                new RenameGroupChatRequest(chatId.getValue(), memberId.getValue(), "New Group");

        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        assertThrows(UnauthorizedOperationException.class, () -> useCase.rename(request));
        verify(groupChatRepository, never()).save(any());
    }
}