package io.github.tony8864.message.usecase.listmessages.dto;

public record ListMessagesRequest(
        String chatId,
        String requesterId,
        int limit
) {
}
