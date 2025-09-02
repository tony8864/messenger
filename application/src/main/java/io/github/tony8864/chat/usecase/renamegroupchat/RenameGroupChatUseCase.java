package io.github.tony8864.chat.usecase.renamegroupchat;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatRequest;
import io.github.tony8864.chat.usecase.renamegroupchat.dto.RenameGroupChatResponse;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.repository.UserRepository;

public class RenameGroupChatUseCase {
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    public RenameGroupChatUseCase(UserRepository userRepository, GroupChatRepository groupChatRepository) {
        this.userRepository = userRepository;
        this.groupChatRepository = groupChatRepository;
    }

    public RenameGroupChatResponse rename(RenameGroupChatRequest request) {
        UserId userId = UserId.of(request.requesterId());
        ChatId chatId = ChatId.of(request.chatId());

        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.getValue()));

        GroupChat chat = groupChatRepository.findById(chatId)
                .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue()
                ));

        boolean isAdmin = chat.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.isAdmin());

        if (!isAdmin) {
            throw new UnauthorizedOperationException("Only admin can rename group chat");
        }

        chat.rename(request.newGroupName());
        groupChatRepository.save(chat);

        return RenameGroupChatResponse.fromDomain(chat);
    }
}
