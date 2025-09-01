package io.github.tony8864.chat.usecase.removeparticipant.dto;

import io.github.tony8864.chat.common.dto.ParticipantDto;
import io.github.tony8864.entities.chat.GroupChat;

import java.util.List;

public record RemoveParticipantResponse(
        String chatId,
        List<ParticipantDto> participantDtos,
        String groupName
) {
    public static RemoveParticipantResponse fromDomain(GroupChat chat) {
        return new RemoveParticipantResponse(
                chat.getChatId().getValue(),
                chat.getParticipants().stream()
                        .map(ParticipantDto::fromDomain)
                        .toList(),
                chat.getGroupName()
        );
    }
}
