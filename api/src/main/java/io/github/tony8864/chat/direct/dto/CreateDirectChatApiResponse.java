package io.github.tony8864.chat.direct.dto;

import java.util.List;

public record CreateDirectChatApiResponse(
        String chatId,
        List<String> participantIds
) {
}