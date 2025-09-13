package io.github.tony8864.chat.usecase.listchats.dto;

import java.util.List;

public record ListChatsResponse(
        List<ChatSummaryDto> chats
) {}
