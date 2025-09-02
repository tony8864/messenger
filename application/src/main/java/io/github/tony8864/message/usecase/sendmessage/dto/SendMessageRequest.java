package io.github.tony8864.message.usecase.sendmessage.dto;

public record SendMessageRequest(
        String chatId,
        String senderId,
        String content
) {
}
