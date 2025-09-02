package io.github.tony8864.message.usecase.listmessages.dto;

import io.github.tony8864.entities.message.Message;

import java.time.Instant;

public record MessageDto(
    String messageId,
    String senderId,
    String content,
    String status,
    Instant createdAt
) {
    public static MessageDto fromDomain(Message message) {
        return new MessageDto(
                message.getMessageId().getValue(),
                message.getUserId().getValue(),
                message.getContent(),
                message.getStatus().name(),
                message.getCreatedAt()
        );
    }
}
