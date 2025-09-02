package io.github.tony8864.chat.usecase.renamegroupchat.dto;

public record RenameGroupChatRequest(
        String chatId,
        String requesterId,
        String newGroupName
) {
}
