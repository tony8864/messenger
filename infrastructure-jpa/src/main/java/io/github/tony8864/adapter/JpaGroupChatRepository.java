package io.github.tony8864.adapter;

import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.GroupChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import io.github.tony8864.mapping.GroupChatMapper;
import io.github.tony8864.repository.SpringDataGroupChatRepository;
import io.github.tony8864.repository.SpringDataMessageRepository;
import io.github.tony8864.repository.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaGroupChatRepository implements GroupChatRepository {

    private final SpringDataGroupChatRepository groupChatRepository;
    private final SpringDataMessageRepository messageRepository;
    private final SpringDataUserRepository userRepository;
    private final GroupChatMapper groupChatMapper;

    @Override
    public Optional<GroupChat> findById(ChatId chatId) {
        return groupChatRepository.findByIdWithParticipants(UUID.fromString(chatId.getValue()))
                .map(groupChatMapper::toDomain);
    }

    @Override
    public List<GroupChat> findByParticipant(UserId userId) {
        List<GroupChatEntity> entities = groupChatRepository.findByParticipant(UUID.fromString(userId.getValue()));
        return entities.stream()
                .map(groupChatMapper::toDomain)
                .toList();
    }

    @Override
    public void save(GroupChat groupChat) {
        MessageEntity lastMessage = null;
        if (groupChat.getLastMessageId() != null) {
            lastMessage = messageRepository.getReferenceById(
                    UUID.fromString(groupChat.getLastMessageId().getValue())
            );
        }

        List<UserEntity> users = groupChat.getParticipants().stream()
                .map(p -> userRepository.getReferenceById(UUID.fromString(p.getUserId().getValue())))
                .toList();

        GroupChatEntity entity = groupChatMapper.fromDomain(groupChat, users, lastMessage);
        groupChatRepository.save(entity);
    }

    @Override
    public void delete(GroupChat groupChat) {
        groupChatRepository.deleteById(UUID.fromString(groupChat.getChatId().getValue()));
    }
}
