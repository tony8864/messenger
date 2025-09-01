package io.github.tony8864.chat.dto;

public record AddParticipantRequest(
        String chatId,
        String requesterId,
        String userId
) {
}
