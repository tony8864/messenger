package io.github.tony8864.chat.group.dto;

import java.time.Instant;
import java.util.List;

public record CreateGroupChatApiResponse(
        String chatId,
        String groupName,
        List<ParticipantApiDto> participants,
        Instant createdAt
) {
}