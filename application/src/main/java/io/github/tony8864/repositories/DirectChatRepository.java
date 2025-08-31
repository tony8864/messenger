package io.github.tony8864.repositories;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;

import java.util.Optional;

public interface DirectChatRepository {
    Optional<DirectChat> findById(ChatId chatId);
    void save(DirectChat directChat);
    void delete(DirectChat directChat);
}
