package io.github.tony8864.message.usecase.listmessages.dto;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;

import java.util.List;

public record ListMessagesResponse(
        String chatId,
        List<MessageDto> messageDtos
) {
    public static ListMessagesResponse fromDomain(ChatId chatId, List<Message> messages) {
        return new ListMessagesResponse(
            chatId.getValue(),
            messages.stream()
                    .map(MessageDto::fromDomain)
                    .toList()
        );
    }
}
