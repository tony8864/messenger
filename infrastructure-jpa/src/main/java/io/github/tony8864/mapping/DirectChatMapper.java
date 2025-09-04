package io.github.tony8864.mapping;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.DirectChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DirectChatMapper {
    public DirectChat toDomain(DirectChatEntity entity) {
        List<UserId> participants = List.of(
                UserId.of(entity.getUser1().getId().toString()),
                UserId.of(entity.getUser2().getId().toString())
        );

        DirectChat chat = DirectChat.create(
                ChatId.of(entity.getId().toString()),
                participants
        );

        if (entity.getLastMessage() != null) {
            chat.updateLastMessage(MessageId.of(entity.getLastMessage().getId().toString()));
        }

        return chat;
    }

    public DirectChatEntity fromDomain(
            DirectChat chat,
            UserEntity user1,
            UserEntity user2,
            MessageEntity lastMessage
    ) {
        return new DirectChatEntity(
                UUID.fromString(chat.getChatId().getValue()),
                user1,
                user2,
                chat.getCreatedAt(),
                lastMessage
        );
    }
}
