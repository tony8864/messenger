package io.github.tony8864.mapping;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.DirectChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DirectChatMapperTest {
    private final DirectChatMapper mapper = new DirectChatMapper();

    @Test
    void toDomain_shouldMapEntityToDomain() {
        // given
        UUID chatId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        UserEntity user1 = new UserEntity(user1Id, "alice", "alice@example.com", "hash1", "ONLINE", Instant.now());
        UserEntity user2 = new UserEntity(user2Id, "bob", "bob@example.com", "hash2", "OFFLINE", Instant.now());
        MessageEntity message = new MessageEntity(messageId, null, user1, Instant.now(), "hello", "SENT", null);

        DirectChatEntity entity = new DirectChatEntity(
                chatId,
                user1,
                user2,
                Instant.now(),
                message
        );

        // when
        DirectChat domain = mapper.toDomain(entity);

        // then
        assertEquals(chatId.toString(), domain.getChatId().getValue());
        assertTrue(domain.getParticipants().containsAll(List.of(
                UserId.of(user1Id.toString()), UserId.of(user2Id.toString())
        )));
        assertEquals(messageId.toString(), domain.getLastMessageId().getValue());
    }

    @Test
    void fromDomain_shouldMapDomainToEntity() {
        // given
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        UserId user1Id = UserId.of(UUID.randomUUID().toString());
        UserId user2Id = UserId.of(UUID.randomUUID().toString());

        DirectChat domain = DirectChat.create(chatId, List.of(user1Id, user2Id));
        MessageId lastMessageId = MessageId.of(UUID.randomUUID().toString());
        domain.updateLastMessage(lastMessageId);

        UserEntity user1 = new UserEntity(UUID.fromString(user1Id.getValue()), "alice", "alice@example.com", "hash1", "ONLINE", Instant.now());
        UserEntity user2 = new UserEntity(UUID.fromString(user2Id.getValue()), "bob", "bob@example.com", "hash2", "OFFLINE", Instant.now());
        MessageEntity message = new MessageEntity(UUID.fromString(lastMessageId.getValue()), null, user1, Instant.now(), "hello", "SENT", null);

        // when
        DirectChatEntity entity = mapper.fromDomain(domain, user1, user2, message);

        // then
        assertEquals(UUID.fromString(chatId.getValue()), entity.getId());
        assertEquals(user1.getId(), entity.getUser1().getId());
        assertEquals(user2.getId(), entity.getUser2().getId());
        assertEquals(message.getId(), entity.getLastMessage().getId());
    }
}