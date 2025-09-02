package io.github.tony8864.message.usecase;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.message.usecase.dto.SendMessageRequest;
import io.github.tony8864.message.usecase.dto.SendMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SendMessageUseCaseTest {
    private MessageRepository messageRepository;
    private GroupChatRepository groupChatRepository;
    private DirectChatRepository directChatRepository;
    private SendMessageUseCase useCase;

    private ChatId chatId;
    private UserId senderId;
    private DirectChat directChat;
    private GroupChat groupChat;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        directChatRepository = mock(DirectChatRepository.class);
        useCase = new SendMessageUseCase(messageRepository, groupChatRepository, directChatRepository);

        chatId = ChatId.of("chat-1");
        senderId = UserId.of("user-1");

        directChat = mock(DirectChat.class);
        when(directChat.getChatId()).thenReturn(chatId);

        groupChat = mock(GroupChat.class);
        when(groupChat.getChatId()).thenReturn(chatId);
    }

    @Test
    void sendMessage_directChat_success() {
        SendMessageRequest request = new SendMessageRequest(chatId.getValue(), senderId.getValue(), "hello!");

        when(directChatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
        when(directChat.canSendMessage(senderId)).thenReturn(true);

        SendMessageResponse response = useCase.send(request);

        assertEquals(request.content(), response.content());
        verify(messageRepository).save(any(Message.class));
        verify(directChatRepository).save(directChat);
    }

    @Test
    void sendMessage_groupChat_success() {
        SendMessageRequest request = new SendMessageRequest(chatId.getValue(), senderId.getValue(), "hello group");

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
        when(groupChat.canSendMessage(senderId)).thenReturn(true);

        SendMessageResponse response = useCase.send(request);

        assertEquals(request.content(), response.content());
        verify(messageRepository).save(any(Message.class));
        verify(groupChatRepository).save(groupChat);
    }

    @Test
    void sendMessage_directChat_unauthorized() {
        SendMessageRequest request = new SendMessageRequest(chatId.getValue(), senderId.getValue(), "bad");

        when(directChatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
        when(directChat.canSendMessage(senderId)).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class, () -> useCase.send(request));
        verify(messageRepository, never()).save(any());
        verify(directChatRepository, never()).save(any());
    }

    @Test
    void sendMessage_groupChat_unauthorized() {
        SendMessageRequest request = new SendMessageRequest(chatId.getValue(), senderId.getValue(), "bad");

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
        when(groupChat.canSendMessage(senderId)).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class, () -> useCase.send(request));
        verify(messageRepository, never()).save(any());
        verify(groupChatRepository, never()).save(any());
    }

    @Test
    void sendMessage_chatNotFoundAnywhere() {
        SendMessageRequest request = new SendMessageRequest(chatId.getValue(), senderId.getValue(), "hello?");

        when(directChatRepository.findById(chatId)).thenReturn(Optional.empty());
        when(groupChatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(GroupChatNotFoundException.class, () -> useCase.send(request));
    }
}