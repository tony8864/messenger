package io.github.tony8864.message.dto;

import java.time.Instant;

public record MessageApiDto(
        String messageId,
        String senderId,
        String content,
        String status,
        Instant createdAt
) {}