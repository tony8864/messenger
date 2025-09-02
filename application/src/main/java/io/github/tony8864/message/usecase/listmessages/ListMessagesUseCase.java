package io.github.tony8864.message.usecase.listmessages;

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
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesRequest;
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesResponse;

import java.util.List;

public class ListMessagesUseCase {
    private final MessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final DirectChatRepository directChatRepository;

    public ListMessagesUseCase(MessageRepository messageRepository, GroupChatRepository groupChatRepository, DirectChatRepository directChatRepository) {
        this.messageRepository = messageRepository;
        this.groupChatRepository = groupChatRepository;
        this.directChatRepository = directChatRepository;
    }

    public ListMessagesResponse list(ListMessagesRequest request) {
        UserId requesterId = UserId.of(request.requesterId());
        ChatId chatId = ChatId.of(request.chatId());

        return directChatRepository.findById(chatId)
                .map(chat -> handleDirectChatMessages(chat, requesterId, request.limit()))
                .orElseGet(() -> groupChatRepository.findById(chatId)
                        .map(chat -> handleGroupChatMessages(chat, requesterId, request.limit()))
                        .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue())));
    }

    private ListMessagesResponse handleDirectChatMessages(DirectChat chat, UserId requesterId, int limit) {
        if (!chat.getParticipants().contains(requesterId)) {
            throw new UnauthorizedOperationException("User is not a participant of this direct chat");
        }

        List<Message> messages = messageRepository.findLastNMessages(chat.getChatId(), limit);
        return ListMessagesResponse.fromDomain(chat.getChatId(), messages);
    }

    private ListMessagesResponse handleGroupChatMessages(GroupChat chat, UserId requesterId, int limit) {
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(requesterId));

        if (!isParticipant) {
            throw new UnauthorizedOperationException("User is not a participant of this group chat");
        }

        List<Message> messages = messageRepository.findLastNMessages(chat.getChatId(), limit);
        return ListMessagesResponse.fromDomain(chat.getChatId(), messages);
    }
}
