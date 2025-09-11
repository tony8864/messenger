package io.github.tony8864.message.mapper;

import io.github.tony8864.message.dto.SendMessageApiRequest;
import io.github.tony8864.message.dto.SendMessageApiResponse;
import io.github.tony8864.message.usecase.sendmessage.dto.SendMessageRequest;
import io.github.tony8864.message.usecase.sendmessage.dto.SendMessageResponse;
import org.springframework.stereotype.Component;

@Component
public class MessageApiMapper {
    public SendMessageRequest toApplication(SendMessageApiRequest apiRequest, String chatId, String senderId) {
        return new SendMessageRequest(chatId, senderId, apiRequest.content());
    }

    public SendMessageApiResponse toApi(SendMessageResponse appResponse) {
        return new SendMessageApiResponse(
                appResponse.messageId(),
                appResponse.chatId(),
                appResponse.senderId(),
                appResponse.content(),
                appResponse.status(),
                appResponse.createdAt().toString(),
                appResponse.updatedAt().toString()
        );
    }
}
