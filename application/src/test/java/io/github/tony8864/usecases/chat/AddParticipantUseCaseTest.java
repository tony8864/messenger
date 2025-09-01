package io.github.tony8864.usecases.chat;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.chat.UserAlreadyParticipantException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.repositories.GroupChatRepository;
import io.github.tony8864.repositories.UserRepository;
import io.github.tony8864.usecases.chat.dto.AddParticipantRequest;
import io.github.tony8864.usecases.chat.dto.AddParticipantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddParticipantUseCaseTest {
    private UserRepository userRepository;
    private GroupChatRepository groupChatRepository;
    private AddParticipantUseCase useCase;

    private UserId requesterId;
    private UserId newUserId;
    private ChatId chatId;
    private User requester;
    private User newUser;
    private GroupChat chat;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        useCase = new AddParticipantUseCase(userRepository, groupChatRepository);

        requesterId = UserId.of("requester-1");
        newUserId = UserId.of("user-2");
        chatId = ChatId.of("chat-123");

        requester = mock(User.class);
        when(requester.getUserId()).thenReturn(requesterId);

        newUser = mock(User.class);
        when(newUser.getUserId()).thenReturn(newUserId);

        Participant admin = Participant.create(requesterId, Role.ADMIN);
        Participant member1 = Participant.create(UserId.of("member-1"), Role.MEMBER);
        Participant member2 = Participant.create(UserId.of("member-2"), Role.MEMBER);

        List<Participant> participants = new ArrayList<>(List.of(admin, member1, member2));
        chat = GroupChat.create(chatId, participants, "Test Group");
    }

    @Test
    void addParticipant_success() {
        // given
        AddParticipantRequest request =
                new AddParticipantRequest(chatId.getValue(), requesterId.getValue(), newUserId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));

        // when
        AddParticipantResponse response = useCase.add(request);

        // then
        assertEquals(chatId.getValue(), response.chatId());
        assertTrue(response.participants().stream()
                        .anyMatch(dto -> dto.userId().equals(newUserId.getValue())),
                "The new user should be included in the response participants list");

        verify(groupChatRepository).save(chat);
    }

    @Test
    void addParticipant_requesterNotAdmin() {
        // given: requester is not admin in this group
        Participant admin = Participant.create(UserId.of("someone-else"), Role.ADMIN);
        Participant member1 = Participant.create(UserId.of("member-1"), Role.MEMBER);
        Participant requesterAsMember = Participant.create(requesterId, Role.MEMBER);

        chat = GroupChat.create(chatId,
                new ArrayList<>(List.of(admin, member1, requesterAsMember)),
                "Test Group");

        AddParticipantRequest request =
                new AddParticipantRequest(chatId.getValue(), requesterId.getValue(), newUserId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));

        // then
        assertThrows(UnauthorizedOperationException.class, () -> useCase.add(request));
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void addParticipant_alreadyExists() {
        // given: newUser is already in the group
        Participant admin = Participant.create(requesterId, Role.ADMIN);
        Participant member1 = Participant.create(UserId.of("member-1"), Role.MEMBER);
        Participant newParticipant = Participant.create(newUserId, Role.MEMBER);

        chat = GroupChat.create(chatId,
                new ArrayList<>(List.of(admin, member1, newParticipant)),
                "Test Group");

        AddParticipantRequest request =
                new AddParticipantRequest(chatId.getValue(), requesterId.getValue(), newUserId.getValue());

        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));

        // then
        assertThrows(UserAlreadyParticipantException.class, () -> useCase.add(request));
        verify(groupChatRepository, never()).save(any());
    }
}