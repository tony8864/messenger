package io.github.tony8864.chat.usecase;

import io.github.tony8864.chat.dto.CreateGroupChatRequest;
import io.github.tony8864.chat.dto.CreateGroupChatResponse;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.common.exception.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatUseCase {
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    public CreateGroupChatUseCase(UserRepository userRepository, GroupChatRepository groupChatRepository) {
        this.userRepository = userRepository;
        this.groupChatRepository = groupChatRepository;
    }

    public CreateGroupChatResponse create(CreateGroupChatRequest request) {
        UserId requesterId = UserId.of(request.requesterId());
        List<UserId> userIds = mapToUserIds(request.userIds());

        userRepository.findById(requesterId)
                .orElseThrow(() -> UserNotFoundException.byId(requesterId.getValue()));

        for (UserId userId : userIds) {
            userRepository.findById(userId)
                    .orElseThrow(() -> UserNotFoundException.byId(userId.getValue()));
        }

        List<Participant> participants = mapToParticipants(userIds, requesterId);
        GroupChat chat = GroupChat.create(ChatId.newId(), participants, request.groupName());
        groupChatRepository.save(chat);

        return CreateGroupChatResponse.fromDomain(chat);
    }

    private List<UserId> mapToUserIds(List<String> userIds) {
        List<UserId> domainIds = new ArrayList<>();
        for (String userId : userIds) {
            domainIds.add(UserId.of(userId));
        }
        return domainIds;
    }

    private List<Participant> mapToParticipants(List<UserId> userIds, UserId requesterId) {
        List<Participant> participants = new ArrayList<>();
        participants.add(Participant.create(requesterId, Role.ADMIN));
        for (UserId userId : userIds) {
            if (!userId.equals(requesterId)) {
                participants.add(Participant.create(userId, Role.MEMBER));
            }
        }
        return participants;
    }
}
