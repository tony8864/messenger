package io.github.tony8864.chat.group.dto;

import java.util.List;

public record AddParticipantApiResponse(
        String chatId,
        String groupName,
        List<ParticipantApiDto> participants
) {
}