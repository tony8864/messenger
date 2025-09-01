package io.github.tony8864.chat.usecase.createdirectchat;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatRequest;
import io.github.tony8864.chat.usecase.createdirectchat.dto.CreateDirectChatResponse;
import io.github.tony8864.chat.usecase.createdirectchat.exception.InvalidChatException;
import io.github.tony8864.chat.usecase.createdirectchat.exception.UniqueConstraintViolationException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class CreateDirectChatUseCase {
    private final UserRepository userRepository;
    private final DirectChatRepository directChatRepository;

    public CreateDirectChatUseCase(UserRepository userRepository, DirectChatRepository directChatRepository) {
        this.userRepository = userRepository;
        this.directChatRepository = directChatRepository;
    }

    public CreateDirectChatResponse create(CreateDirectChatRequest request) {
        UserId requesterId = UserId.of(request.requesterId());
        UserId otherId = UserId.of(request.otherUserId());

        if (requesterId.equals(otherId)) {
            throw new InvalidChatException("Cannot create a direct chat with yourself");
        }

        userRepository.findById(requesterId)
                .orElseThrow(() -> UserNotFoundException.byId(requesterId.getValue()));

        userRepository.findById(otherId)
                .orElseThrow(() -> UserNotFoundException.byId(otherId.getValue()));

        UserId user1 = requesterId.getValue().compareTo(otherId.getValue()) < 0
                ? requesterId : otherId;
        UserId user2 = user1.equals(requesterId) ? otherId : requesterId;

        Optional<DirectChat> existing = directChatRepository.findByUsers(user1, user2);
        if (existing.isPresent()) {
            return CreateDirectChatResponse.fromDomain(existing.get());
        }

        DirectChat chat = DirectChat.create(ChatId.newId(), List.of(user1, user2));

        try {
            directChatRepository.save(chat);
            return CreateDirectChatResponse.fromDomain(chat);
        } catch (UniqueConstraintViolationException e) {
            DirectChat winner = directChatRepository.findByUsers(user1, user2)
                    .orElseThrow(() -> new IllegalStateException("Chat should exist now"));
            return CreateDirectChatResponse.fromDomain(winner);
        }
    }
}
