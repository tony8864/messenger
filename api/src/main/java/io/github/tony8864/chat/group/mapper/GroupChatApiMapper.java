package io.github.tony8864.chat.group.mapper;

import io.github.tony8864.chat.group.dto.*;
import io.github.tony8864.chat.usecase.removeparticipant.dto.RemoveParticipantRequest;
import io.github.tony8864.chat.usecase.removeparticipant.dto.RemoveParticipantResponse;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatRequest;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatResponse;
import org.springframework.stereotype.Component;

@Component
public class GroupChatApiMapper {

    // --- Remove Participant ---
    public RemoveParticipantRequest toApplication(RemoveParticipantApiRequest apiRequest, String requesterId) {
        return new RemoveParticipantRequest(
                apiRequest.chatId(),
                requesterId,
                apiRequest.removeUserId()
        );
    }

    public RemoveParticipantApiResponse toApi(RemoveParticipantResponse appResponse) {
        return new RemoveParticipantApiResponse(
                appResponse.chatId(),
                appResponse.participantDtos().stream()
                        .map(p -> new ParticipantApiDto(p.userId(), p.role().name()))
                        .toList(),
                appResponse.groupName()
        );
    }

    // --- Rename Group Chat ---
    public RenameGroupChatRequest toApplication(RenameGroupChatApiRequest apiRequest, String requesterId) {
        return new RenameGroupChatRequest(
                apiRequest.chatId(),
                requesterId,
                apiRequest.newGroupName()
        );
    }

    public RenameGroupChatApiResponse toApi(RenameGroupChatResponse appResponse) {
        return new RenameGroupChatApiResponse(
                appResponse.chatId(),
                appResponse.groupName(),
                appResponse.participantDtos().stream()
                        .map(p -> new ParticipantApiDto(
                                p.userId(),
                                p.role().name()
                        ))
                        .toList()
        );
    }
}
