package io.github.tony8864.message.dto;

import java.util.List;

public record ListMessagesApiResponse(
        String chatId,
        List<MessageApiDto> messages
) {}