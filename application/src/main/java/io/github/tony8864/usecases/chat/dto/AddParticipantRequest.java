package io.github.tony8864.usecases.chat.dto;

public record AddParticipantRequest(
        String chatId,
        String requesterId,
        String userId
) {
}
