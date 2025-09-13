package io.github.tony8864.adapter;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entity.ChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import io.github.tony8864.mapping.MessageMapper;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.repository.SpringDataGroupChatRepository;
import io.github.tony8864.repository.SpringDataMessageRepository;
import io.github.tony8864.repository.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaMessageRepository implements MessageRepository {

    private final SpringDataMessageRepository messageRepository;
    private final SpringDataUserRepository userRepository;
    private final EntityManager entityManager;
    private final MessageMapper messageMapper;

    @Override
    public List<Message> findLastNMessages(ChatId chatId, int limit) {
        return messageRepository.findByChat_IdOrderByCreatedAtDescIdDesc(
                UUID.fromString(chatId.getValue()),
                PageRequest.of(0, limit)
        )
                .stream()
                .map(messageMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Message> findLastMessage(ChatId chatId) {
        return Optional.empty();
    }

    @Override
    public void save(Message message) {
        ChatEntity chat = entityManager.getReference(
                ChatEntity.class,
                UUID.fromString(message.getChatId().getValue())
        );

        UserEntity user = userRepository.getReferenceById(
                UUID.fromString(message.getUserId().getValue())
        );

        MessageEntity entity = messageMapper.fromDomain(message, chat, user);
        messageRepository.save(entity);
    }
}
