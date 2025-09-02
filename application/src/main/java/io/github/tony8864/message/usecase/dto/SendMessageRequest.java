package io.github.tony8864.message.usecase.dto;

public record SendMessageRequest(
        String chatId,
        String senderId,
        String content
) {
}
