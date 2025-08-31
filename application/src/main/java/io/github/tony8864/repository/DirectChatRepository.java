package io.github.tony8864.repository;

import io.github.tony8864.chat.ChatId;
import io.github.tony8864.chat.DirectChat;

import java.util.Optional;

public interface DirectChatRepository {
    Optional<DirectChat> findById(ChatId chatId);
    void save(DirectChat directChat);
    void delete(DirectChat directChat);
}
