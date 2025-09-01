package io.github.tony8864.chat.dto;

import io.github.tony8864.entities.chat.GroupChat;

import java.util.List;

public record AddParticipantResponse(
        String chatId,
        List<ParticipantDto> participants,
        String groupName
) {
    public static AddParticipantResponse fromDomain(GroupChat chat) {
        return new AddParticipantResponse(
                chat.getChatId().getValue(),
                chat.getParticipants().stream()
                        .map(ParticipantDto::fromDomain)
                        .toList(),
                chat.getGroupName()
        );
    }
}
