package io.github.tony8864.chat.usecase.listchats.dto;

import java.time.Instant;

public record ChatSummaryDto(
        String chatId,
        String type,
        String name,
        String lastMessage,
        Instant lastMessageAt
) {}
