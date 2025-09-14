package io.github.tony8864.chat.usecase.listchats;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.listchats.dto.ChatSummaryDto;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsRequest;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsResponse;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListChatsUseCase {
    private final DirectChatRepository directChatRepository;
    private final GroupChatRepository groupChatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ListChatsUseCase(
            DirectChatRepository directChatRepository,
            GroupChatRepository groupChatRepository,
            MessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.directChatRepository = directChatRepository;
        this.groupChatRepository = groupChatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public ListChatsResponse list(ListChatsRequest request) {
        UserId requesterId = UserId.of(request.requesterId());

        List<ChatSummaryDto> directChatSummaries =
                directChatRepository.findByParticipant(requesterId).stream()
                        .map(chat -> toDirectChatSummary(chat, requesterId))
                        .toList();

        List<ChatSummaryDto> groupChatSummaries =
                groupChatRepository.findByParticipant(requesterId).stream()
                        .map(this::toGroupChatSummary)
                        .toList();

        // merge and sort by last message time
        List<ChatSummaryDto> allChats = new ArrayList<>();
        allChats.addAll(directChatSummaries);
        allChats.addAll(groupChatSummaries);

        allChats.sort(Comparator.comparing(ChatSummaryDto::lastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())));

        // apply limit if set
        List<ChatSummaryDto> limited = request.limit() > 0
                ? allChats.stream().limit(request.limit()).toList()
                : allChats;

        return new ListChatsResponse(limited);
    }

    private ChatSummaryDto toDirectChatSummary(DirectChat chat, UserId requesterId) {
        // pick the other participant’s name as the chat "name"
        UserId other = chat.getParticipants().stream()
                .filter(p -> !p.equals(requesterId))
                .findFirst()
                .orElseThrow();

        var otherUser = userRepository.findById(other)
                .orElseThrow(() -> UserNotFoundException.byId(other.getValue()));

        // fetch latest message (optional)
        Message lastMessage = messageRepository.findLastMessage(chat.getChatId()).orElse(null);

        return new ChatSummaryDto(
                chat.getChatId().getValue(),
                "DIRECT",
                otherUser.getUsername(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : null
        );
    }

    private ChatSummaryDto toGroupChatSummary(GroupChat chat) {
        Message lastMessage = messageRepository.findLastMessage(chat.getChatId()).orElse(null);

        return new ChatSummaryDto(
                chat.getChatId().getValue(),
                "GROUP",
                chat.getGroupName(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : null
        );
    }
}
