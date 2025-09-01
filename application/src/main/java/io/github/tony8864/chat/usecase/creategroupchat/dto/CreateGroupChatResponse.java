package io.github.tony8864.chat.usecase.creategroupchat.dto;

import io.github.tony8864.chat.common.dto.ParticipantDto;
import io.github.tony8864.entities.chat.GroupChat;

import java.time.Instant;
import java.util.List;

public record CreateGroupChatResponse(
        String chatId,
        String groupName,
        List<ParticipantDto> participantDtos,
        Instant createdAt
) {
    public static CreateGroupChatResponse fromDomain(GroupChat chat) {
        return new CreateGroupChatResponse(
                chat.getChatId().getValue(),
                chat.getGroupName(),
                chat.getParticipants().stream()
                        .map(ParticipantDto::fromDomain)
                        .toList(),
                chat.getCreatedAt()
        );
    }
}
