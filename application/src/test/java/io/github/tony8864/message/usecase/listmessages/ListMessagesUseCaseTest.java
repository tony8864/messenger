package io.github.tony8864.message.usecase.listmessages;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesRequest;
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListMessagesUseCaseTest {
    private MessageRepository messageRepository;
    private GroupChatRepository groupChatRepository;
    private DirectChatRepository directChatRepository;
    private ListMessagesUseCase useCase;

    private ChatId chatId;
    private UserId requesterId;
    private DirectChat directChat;
    private GroupChat groupChat;
    private Message message1;
    private Message message2;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        directChatRepository = mock(DirectChatRepository.class);
        useCase = new ListMessagesUseCase(messageRepository, groupChatRepository, directChatRepository);

        chatId = ChatId.of("chat-1");
        requesterId = UserId.of("user-1");

        directChat = mock(DirectChat.class);
        when(directChat.getChatId()).thenReturn(chatId);

        groupChat = mock(GroupChat.class);
        when(groupChat.getChatId()).thenReturn(chatId);

        message1 = Message.create(MessageId.newId(), chatId, requesterId, "Hello");
        message2 = Message.create(MessageId.newId(), chatId, requesterId, "World");
    }

    @Test
    void listMessages_directChat_success() {
        ListMessagesRequest request = new ListMessagesRequest(chatId.getValue(), requesterId.getValue(), 2);

        when(directChatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
        when(directChat.getParticipants()).thenReturn(List.of(requesterId));
        when(messageRepository.findLastNMessages(chatId, 2)).thenReturn(List.of(message1, message2));

        ListMessagesResponse response = useCase.list(request);

        assertEquals(2, response.messageDtos().size());
        assertEquals("Hello", response.messageDtos().get(0).content());
        verify(messageRepository).findLastNMessages(chatId, 2);
    }


    @Test
    void listMessages_directChat_unauthorized() {
        ListMessagesRequest request = new ListMessagesRequest(chatId.getValue(), requesterId.getValue(), 1);

        when(directChatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
        when(directChat.getParticipants()).thenReturn(List.of(UserId.of("other-user")));

        assertThrows(UnauthorizedOperationException.class, () -> useCase.list(request));
        verify(messageRepository, never()).findLastNMessages(any(), anyInt());
    }

    @Test
    void listMessages_groupChat_success() {
        ListMessagesRequest request = new ListMessagesRequest(chatId.getValue(), requesterId.getValue(), 2);

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
        when(groupChat.getParticipants()).thenReturn(List.of(
                Participant.create(requesterId, Role.MEMBER),
                Participant.create(UserId.of("other"), Role.MEMBER)
        ));
        when(messageRepository.findLastNMessages(chatId, 2)).thenReturn(List.of(message1, message2));

        ListMessagesResponse response = useCase.list(request);

        assertEquals(2, response.messageDtos().size());
        assertEquals("World", response.messageDtos().get(1).content());
        verify(messageRepository).findLastNMessages(chatId, 2);
    }

    @Test
    void listMessages_groupChat_unauthorized() {
        ListMessagesRequest request = new ListMessagesRequest(chatId.getValue(), requesterId.getValue(), 1);

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
        when(groupChat.getParticipants()).thenReturn(List.of(
                Participant.create(UserId.of("someone-else"), Role.MEMBER)
        ));

        assertThrows(UnauthorizedOperationException.class, () -> useCase.list(request));
        verify(messageRepository, never()).findLastNMessages(any(), anyInt());
    }

    @Test
    void listMessages_chatNotFoundAnywhere() {
        ListMessagesRequest request = new ListMessagesRequest(chatId.getValue(), requesterId.getValue(), 1);

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(GroupChatNotFoundException.class, () -> useCase.list(request));
    }
}