package io.github.tony8864.chat.repository;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;

import java.util.Optional;

public interface GroupChatRepository {
    Optional<GroupChat> findById(ChatId chatId);
    void save(GroupChat groupChat);
    void delete(GroupChat groupChat);
}
