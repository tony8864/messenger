package io.github.tony8864.chat.group.mapper;

import io.github.tony8864.chat.group.dto.ParticipantApiDto;
import io.github.tony8864.chat.group.dto.RenameGroupChatApiRequest;
import io.github.tony8864.chat.group.dto.RenameGroupChatApiResponse;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatRequest;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatResponse;
import org.springframework.stereotype.Component;

@Component
public class GroupChatApiMapper {

    public RenameGroupChatRequest toApplication(RenameGroupChatApiRequest apiRequest) {
        return new RenameGroupChatRequest(
                apiRequest.chatId(),
                apiRequest.requesterId(),
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
