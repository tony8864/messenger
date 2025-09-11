package io.github.tony8864.publisher;

import io.github.tony8864.dto.ChatMessageDto;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.message.repository.MessageEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WebSocketMessagePublisher implements MessageEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishMessageSent(Message message) {
        ChatMessageDto dto = new ChatMessageDto(
                message.getMessageId().getValue(),
                message.getChatId().getValue(),
                message.getUserId().getValue(),
                message.getContent(),
                message.getCreatedAt().toString()
        );

        messagingTemplate.convertAndSend(
                "/topic/chats/" + message.getChatId().getValue() + "/messages",
                dto
        );
    }
}
