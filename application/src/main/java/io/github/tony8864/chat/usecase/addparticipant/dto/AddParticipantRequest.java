package io.github.tony8864.chat.usecase.addparticipant.dto;

public record AddParticipantRequest(
        String chatId,
        String requesterId,
        String userId
) {
}
