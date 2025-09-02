package io.github.tony8864.chat.usecase.deletegroupchat;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.deletegroupchat.dto.DeleteGroupChatRequest;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.repository.UserRepository;

public class DeleteGroupChatUseCase {

    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    public DeleteGroupChatUseCase(UserRepository userRepository, GroupChatRepository groupChatRepository) {
        this.userRepository = userRepository;
        this.groupChatRepository = groupChatRepository;
    }

    public void delete(DeleteGroupChatRequest request) {
        UserId userId = UserId.of(request.requesterId());
        ChatId chatId = ChatId.of(request.chatId());

        GroupChat chat = groupChatRepository.findById(chatId)
                .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue()));

        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.getValue()));

        boolean isAdmin = chat.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.isAdmin());

        if (!isAdmin) {
            throw new UnauthorizedOperationException("Only admin can delete group chat");
        }

        groupChatRepository.delete(chat);
    }
}
