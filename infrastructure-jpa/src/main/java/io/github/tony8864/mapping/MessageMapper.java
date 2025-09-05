package io.github.tony8864.mapping;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.message.MessageStatus;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.ChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageMapper {

    public Message toDomain(MessageEntity entity) {
        return Message.restore(
                MessageId.of(entity.getId().toString()),
                ChatId.of(entity.getChat().getId().toString()),
                UserId.of(entity.getUser().getId().toString()),
                entity.getContent(),
                entity.getCreatedAt(),
                MessageStatus.valueOf(entity.getStatus()),
                entity.getUpdatedAt()
        );
    }

    public MessageEntity fromDomain(
            Message message,
            ChatEntity chat,
            UserEntity user
    ) {
        return MessageEntity.builder()
                .id(UUID.fromString(message.getMessageId().getValue()))
                .chat(chat)
                .user(user)
                .createdAt(message.getCreatedAt())
                .content(message.getContent())
                .status(message.getStatus().name())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
