package io.github.tony8864.message.usecase.dto;

import io.github.tony8864.entities.message.Message;

import java.time.Instant;

public record SendMessageResponse(
        String messageId,
        String chatId,
        String senderId,
        String content,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SendMessageResponse fromDomain(Message message) {
        return new SendMessageResponse(
                message.getMessageId().getValue(),
                message.getChatId().getValue(),
                message.getUserId().getValue(),
                message.getContent(),
                message.getStatus().name(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
