package io.github.tony8864.chat.group.dto;

public record RemoveParticipantApiRequest(
        String chatId,
        String removeUserId
) {}
