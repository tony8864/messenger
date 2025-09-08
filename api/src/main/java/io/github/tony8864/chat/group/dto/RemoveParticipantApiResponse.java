package io.github.tony8864.chat.group.dto;

import java.util.List;

public record RemoveParticipantApiResponse(
        String chatId,
        List<ParticipantApiDto> participants,
        String groupName
) {}
