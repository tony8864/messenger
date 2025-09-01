package io.github.tony8864.chat.usecase.removeparticipant.dto;

public record RemoveParticipantRequest(
        String chatId,
        String requesterId,
        String removeUserId
) {
}
