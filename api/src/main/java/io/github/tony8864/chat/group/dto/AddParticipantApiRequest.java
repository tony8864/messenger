package io.github.tony8864.chat.group.dto;

public record AddParticipantApiRequest(
        String chatId,
        String userId
) {
}