package io.github.tony8864.chat.common.dto;

import java.util.List;

public record ListChatsApiResponse(
        List<ChatSummaryApiDto> chats
) {}
