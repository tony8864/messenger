package io.github.tony8864.chat.usecase.createdirectchat.dto;

public record CreateDirectChatRequest(
        String requesterId,
        String otherUserId
) {
}
