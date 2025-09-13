package io.github.tony8864.chat.usecase.listchats.dto;

public record ListChatsRequest(
        String requesterId,
        int limit
) {}
