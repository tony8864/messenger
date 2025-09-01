package io.github.tony8864.chat.dto;

public record RemoveParticipantRequest(
        String chatId,
        String requesterId,
        String removeUserId
) {
}
