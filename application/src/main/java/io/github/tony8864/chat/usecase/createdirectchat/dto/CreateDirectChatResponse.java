package io.github.tony8864.chat.usecase.createdirectchat.dto;

import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.UserId;

import java.util.List;

public record CreateDirectChatResponse(
        String chatId,
        List<String> participantIds
) {
    public static CreateDirectChatResponse fromDomain(DirectChat chat) {
        return new CreateDirectChatResponse(
                chat.getChatId().getValue(),
                chat.getParticipants().stream()
                        .map(UserId::getValue)
                        .toList()
        );
    }
}
