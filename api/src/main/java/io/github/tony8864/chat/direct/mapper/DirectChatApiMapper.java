package io.github.tony8864.chat.direct.mapper;

import io.github.tony8864.chat.direct.dto.CreateDirectChatApiRequest;
import io.github.tony8864.chat.direct.dto.CreateDirectChatApiResponse;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatRequest;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatResponse;
import org.springframework.stereotype.Component;

@Component
public class DirectChatApiMapper {
    // --- Create Direct Chat ---
    public CreateDirectChatRequest toApplication(CreateDirectChatApiRequest apiRequest, String requesterId) {
        return new CreateDirectChatRequest(
                requesterId,
                apiRequest.otherUserId()
        );
    }

    public CreateDirectChatApiResponse toApi(CreateDirectChatResponse appResponse) {
        return new CreateDirectChatApiResponse(
                appResponse.chatId(),
                appResponse.participantIds()
        );
    }
}
