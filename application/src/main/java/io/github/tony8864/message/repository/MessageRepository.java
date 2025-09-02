package io.github.tony8864.message.repository;

import io.github.tony8864.entities.message.Message;

public interface MessageRepository {
    void save(Message message);
}
