package io.github.tony8864.chat.usecase.creategroupchat.dto;

import java.util.List;

public record CreateGroupChatRequest(
        String requesterId,
        String groupName,
        List<String> userIds
) {
}
