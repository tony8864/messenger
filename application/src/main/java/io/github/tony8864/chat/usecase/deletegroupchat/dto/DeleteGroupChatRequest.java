package io.github.tony8864.chat.usecase.deletegroupchat.dto;

public record DeleteGroupChatRequest(
        String chatId,
        String requesterId
) {
}
