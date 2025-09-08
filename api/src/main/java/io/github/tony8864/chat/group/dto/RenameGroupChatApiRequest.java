package io.github.tony8864.chat.group.dto;

public record RenameGroupChatApiRequest(
        String chatId,
        String newGroupName
) {}