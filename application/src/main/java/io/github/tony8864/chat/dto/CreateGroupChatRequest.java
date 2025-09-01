package io.github.tony8864.chat.dto;

import java.util.List;

public record CreateGroupChatRequest(
        String requesterId,
        String groupName,
        List<String> userIds
) {
}
