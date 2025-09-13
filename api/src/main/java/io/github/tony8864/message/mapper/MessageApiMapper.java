package io.github.tony8864.message.mapper;

import io.github.tony8864.message.dto.ListMessagesApiResponse;
import io.github.tony8864.message.dto.MessageApiDto;
import io.github.tony8864.message.dto.SendMessageApiRequest;
import io.github.tony8864.message.dto.SendMessageApiResponse;
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesRequest;
import io.github.tony8864.message.usecase.listmessages.dto.ListMessagesResponse;
import io.github.tony8864.message.usecase.listmessages.dto.MessageDto;
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

    public ListMessagesRequest toApplication(String chatId, String requesterId, int limit) {
        return new ListMessagesRequest(chatId, requesterId, limit);
    }

    public ListMessagesApiResponse toApi(ListMessagesResponse response) {
        return new ListMessagesApiResponse(
                response.chatId(),
                response.messageDtos().stream()
                        .map(this::toApi)
                        .toList()
        );
    }

    private MessageApiDto toApi(MessageDto dto) {
        return new MessageApiDto(
                dto.messageId(),
                dto.senderId(),
                dto.content(),
                dto.status(),
                dto.createdAt()
        );
    }
}
