package io.github.tony8864.chat.usecase;

import io.github.tony8864.chat.dto.RemoveParticipantRequest;
import io.github.tony8864.chat.dto.RemoveParticipantResponse;
import io.github.tony8864.chat.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.common.exception.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;

public class RemoveParticipantUseCase {
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    public RemoveParticipantUseCase(
            UserRepository userRepository,
            GroupChatRepository groupChatRepository
    ) {
        this.userRepository = userRepository;
        this.groupChatRepository = groupChatRepository;
    }
    public RemoveParticipantResponse remove(RemoveParticipantRequest request) {
        UserId requesterId = UserId.of(request.requesterId());
        UserId removeUserId = UserId.of(request.removeUserId());
        ChatId chatId = ChatId.of(request.chatId());

        GroupChat chat = groupChatRepository.findById(chatId)
                .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue()));

        userRepository.findById(requesterId)
                .orElseThrow(() -> UserNotFoundException.byId(requesterId.getValue()));

        userRepository.findById(removeUserId)
                .orElseThrow(() -> UserNotFoundException.byId(removeUserId.getValue()));

        chat.removeParticipant(requesterId, removeUserId);

        groupChatRepository.save(chat);
        return RemoveParticipantResponse.fromDomain(chat);
    }
}
