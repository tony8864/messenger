package io.github.tony8864.message.usecase;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.message.usecase.dto.SendMessageRequest;
import io.github.tony8864.message.usecase.dto.SendMessageResponse;

public class SendMessageUseCase {
    private final MessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final DirectChatRepository directChatRepository;

    public SendMessageUseCase(MessageRepository messageRepository, GroupChatRepository groupChatRepository, DirectChatRepository directChatRepository) {
        this.messageRepository = messageRepository;
        this.groupChatRepository = groupChatRepository;
        this.directChatRepository = directChatRepository;
    }

    public SendMessageResponse send(SendMessageRequest request) {
        ChatId chatId = ChatId.of(request.chatId());
        UserId senderId = UserId.of(request.senderId());

        return directChatRepository.findById(chatId)
                .map(chat -> handleDirectChatMessage(chat, senderId, request.content()))
                .orElseGet(() -> groupChatRepository.findById(chatId)
                        .map(chat -> handleGroupChatMessage(chat, senderId, request.content()))
                        .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue())));
    }

    private SendMessageResponse handleDirectChatMessage(DirectChat chat, UserId senderId, String content) {
        if (!chat.canSendMessage(senderId)) {
            throw new UnauthorizedOperationException("Sender is not in this direct chat");
        }

        Message message = Message.create(MessageId.newId(), chat.getChatId(), senderId, content);
        messageRepository.save(message);
        chat.updateLastMessage(message.getMessageId());
        directChatRepository.save(chat);

        return SendMessageResponse.fromDomain(message);
    }

    private SendMessageResponse handleGroupChatMessage(GroupChat chat, UserId senderId, String content) {
        if (!chat.canSendMessage(senderId)) {
            throw new UnauthorizedOperationException("Sender is not in this direct chat");
        }

        Message message = Message.create(MessageId.newId(), chat.getChatId(), senderId, content);
        messageRepository.save(message);
        chat.updateLastMessage(message.getMessageId());
        groupChatRepository.save(chat);

        return SendMessageResponse.fromDomain(message);
    }
}
