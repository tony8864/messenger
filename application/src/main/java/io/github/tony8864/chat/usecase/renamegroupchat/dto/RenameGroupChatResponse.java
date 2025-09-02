package io.github.tony8864.chat.usecase.renamegroupchat.dto;

import io.github.tony8864.chat.common.dto.ParticipantDto;
import io.github.tony8864.entities.chat.GroupChat;

import java.util.List;

public record RenameGroupChatResponse(
        String chatId,
        String groupName,
        List<ParticipantDto> participantDtos
) {
    public static RenameGroupChatResponse fromDomain(GroupChat chat) {
        return new RenameGroupChatResponse(
                chat.getChatId().getValue(),
                chat.getGroupName(),
                chat.getParticipants().stream()
                        .map(ParticipantDto::fromDomain)
                        .toList()
        );
    }
}
