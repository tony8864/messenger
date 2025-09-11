package io.github.tony8864.message.dto;

public record SendMessageApiResponse(
        String id,
        String chatId,
        String senderId,
        String content,
        String status,
        String createdAt,
        String updatedAt
) {}