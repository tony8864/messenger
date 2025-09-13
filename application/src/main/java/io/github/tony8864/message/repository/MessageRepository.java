package io.github.tony8864.message.repository;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    List<Message> findLastNMessages(ChatId chatId, int limit);
    Optional<Message> findLastMessage(ChatId chatId);
    void save(Message message);
}
