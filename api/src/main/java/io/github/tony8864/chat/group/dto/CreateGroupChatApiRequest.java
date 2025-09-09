package io.github.tony8864.chat.group.dto;

import java.util.List;

public record CreateGroupChatApiRequest(
        String groupName,
        List<String> userIds
) {
}