package io.github.tony8864.chat.repository;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.UserId;

import java.util.Optional;

public interface DirectChatRepository {
    Optional<DirectChat> findById(ChatId chatId);
    Optional<DirectChat> findByUsers(UserId user1, UserId user2);
    void save(DirectChat directChat);
    void delete(DirectChat directChat);
}
