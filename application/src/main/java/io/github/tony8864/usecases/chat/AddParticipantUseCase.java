package io.github.tony8864.usecases.chat;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.GroupChatNotFoundException;
import io.github.tony8864.exceptions.UserNotFoundException;
import io.github.tony8864.repositories.GroupChatRepository;
import io.github.tony8864.repositories.UserRepository;
import io.github.tony8864.usecases.chat.dto.AddParticipantRequest;
import io.github.tony8864.usecases.chat.dto.AddParticipantResponse;

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

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> UserNotFoundException.byId(requesterId.getValue()));

        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.getValue()));

        Participant participant = Participant.create(userId, Role.MEMBER);
        chat.addParticipant(requesterId, participant);

        groupChatRepository.save(chat);
        return AddParticipantResponse.from(chat);
    }
}
