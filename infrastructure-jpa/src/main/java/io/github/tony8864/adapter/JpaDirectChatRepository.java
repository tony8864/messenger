package io.github.tony8864.adapter;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.DirectChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import io.github.tony8864.mapping.DirectChatMapper;
import io.github.tony8864.repository.SpringDataDirectChatRepository;
import io.github.tony8864.repository.SpringDataMessageRepository;
import io.github.tony8864.repository.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@AllArgsConstructor
public class JpaDirectChatRepository implements DirectChatRepository {

    private final SpringDataDirectChatRepository chatRepository;
    private final SpringDataMessageRepository messageRepository;
    private final SpringDataUserRepository userRepository;
    private final DirectChatMapper directChatMapper;

    @Override
    public Optional<DirectChat> findById(ChatId chatId) {
        return chatRepository.findById(UUID.fromString(chatId.getValue()))
                .map(directChatMapper::toDomain);
    }

    @Override
    public Optional<DirectChat> findByUsers(UserId user1, UserId user2) {
        return chatRepository.findByUsers(UUID.fromString(user1.getValue()), UUID.fromString(user2.getValue()))
                .map(directChatMapper::toDomain);
    }

    @Override
    public List<DirectChat> findByParticipant(UserId userId) {
        return List.of();
    }

    @Override
    public void save(DirectChat directChat) {
        UserEntity user1 = userRepository.getReferenceById(UUID.fromString(directChat.getParticipants().get(0).getValue()));
        UserEntity user2 = userRepository.getReferenceById(UUID.fromString(directChat.getParticipants().get(1).getValue()));
        MessageEntity lastMessage = null;

        if (directChat.getLastMessageId() != null) {
            lastMessage = messageRepository.getReferenceById(UUID.fromString(directChat.getLastMessageId().getValue()));
        }

        chatRepository.save(directChatMapper.fromDomain(directChat, user1, user2, lastMessage));
    }

    @Override
    public void delete(DirectChat directChat) {
        chatRepository.deleteById(UUID.fromString(directChat.getChatId().getValue()));
    }
}
