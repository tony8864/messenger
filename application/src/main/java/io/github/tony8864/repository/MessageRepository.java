package io.github.tony8864.repository;

import io.github.tony8864.chat.ChatId;
import io.github.tony8864.message.Message;
import io.github.tony8864.message.MessageId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Optional<Message> findById(MessageId messageId);
    List<Message> findByChatId(ChatId chatId);
    void save(Message message);
}
