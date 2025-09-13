package io.github.tony8864.chat.common.mapper;

import io.github.tony8864.chat.common.dto.ChatSummaryApiDto;
import io.github.tony8864.chat.common.dto.ListChatsApiResponse;
import io.github.tony8864.chat.usecase.listchats.dto.ChatSummaryDto;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsRequest;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommonChatMapper {

    public ListChatsRequest toApplication(String requesterId, int limit) {
        return new ListChatsRequest(requesterId, limit);
    }

    public ListChatsApiResponse toApi(ListChatsResponse appResponse) {
        List<ChatSummaryApiDto> chats = appResponse.chats().stream()
                .map(this::toApi)
                .toList();

        return new ListChatsApiResponse(chats);
    }

    private ChatSummaryApiDto toApi(ChatSummaryDto dto) {
        return new ChatSummaryApiDto(
                dto.chatId(),
                dto.type(),
                dto.name(),
                dto.lastMessage(),
                dto.lastMessageAt()
        );
    }
}
