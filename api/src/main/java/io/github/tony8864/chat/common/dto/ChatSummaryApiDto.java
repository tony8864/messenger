package io.github.tony8864.chat.common.dto;

import java.time.Instant;

public record ChatSummaryApiDto(
        String chatId,
        String type,
        String name,
        String lastMessage,
        Instant lastMessageAt
) {}
