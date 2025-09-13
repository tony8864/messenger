package io.github.tony8864.chat.repository;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.user.UserId;

import java.util.List;
import java.util.Optional;

public interface GroupChatRepository {
    Optional<GroupChat> findById(ChatId chatId);
    List<GroupChat> findByParticipant(UserId userId);
    void save(GroupChat groupChat);
    void delete(GroupChat groupChat);
}
