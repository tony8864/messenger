package io.github.tony8864.chat.usecase;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.chat.exception.GroupChatNotFoundException;
import io.github.tony8864.common.exception.UserNotFoundException;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.chat.dto.AddParticipantRequest;
import io.github.tony8864.chat.dto.AddParticipantResponse;

public class AddParticipantUseCase {

    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    public AddParticipantUseCase(
            UserRepository userRepository,
            GroupChatRepository groupChatRepository
    ) {
            this.userRepository = userRepository;
            this.groupChatRepository = groupChatRepository;
    }

    public AddParticipantResponse add(AddParticipantRequest request) {
        UserId requesterId = UserId.of(request.requesterId());
        UserId userId = UserId.of(request.userId());
        ChatId chatId = ChatId.of(request.chatId());

        GroupChat chat = groupChatRepository.findById(chatId)
                .orElseThrow(() -> new GroupChatNotFoundException(chatId.getValue()));

        userRepository.findById(requesterId)
                .orElseThrow(() -> UserNotFoundException.byId(requesterId.getValue()));

        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.getValue()));

        Participant participant = Participant.create(userId, Role.MEMBER);
        chat.addParticipant(requesterId, participant);

        groupChatRepository.save(chat);
        return AddParticipantResponse.from(chat);
    }
}
