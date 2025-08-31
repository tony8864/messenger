package io.github.tony8864.repositories;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Optional<Message> findById(MessageId messageId);
    List<Message> findByChatId(ChatId chatId);
    void save(Message message);
}
