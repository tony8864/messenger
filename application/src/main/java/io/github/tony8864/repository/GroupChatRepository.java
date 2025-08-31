package io.github.tony8864.repository;

import io.github.tony8864.chat.ChatId;
import io.github.tony8864.chat.GroupChat;

import java.util.Optional;

public interface GroupChatRepository {
    Optional<ChatId> findById(ChatId chatId);
    void save(GroupChat groupChat);
    void delete(GroupChat groupChat);
}
