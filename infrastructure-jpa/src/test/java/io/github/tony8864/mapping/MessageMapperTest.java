package io.github.tony8864.mapping;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.message.MessageStatus;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.ChatEntity;
import io.github.tony8864.entity.GroupChatEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageMapperTest {
    private final MessageMapper mapper = new MessageMapper();

    @Test
    void toDomain_shouldMapEntityToDomain() {
        // given
        UUID messageId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String content = "Hello world";

        ChatEntity chat = new GroupChatEntity(
                chatId,
                "Test Group",
                "ACTIVE",
                createdAt,
                null,
                java.util.Collections.emptyList()
        );

        UserEntity user = UserEntity.builder()
                .id(userId)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("secret")
                .createdAt(createdAt)
                .status("ACTIVE")
                .build();

        MessageEntity entity = MessageEntity.builder()
                .id(messageId)
                .chat(chat)
                .user(user)
                .createdAt(createdAt)
                .content(content)
                .status(MessageStatus.SENT.name())
                .updatedAt(updatedAt)
                .build();

        // when
        Message domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getMessageId().getValue()).isEqualTo(messageId.toString());
        assertThat(domain.getChatId().getValue()).isEqualTo(chatId.toString());
        assertThat(domain.getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(domain.getContent()).isEqualTo(content);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void fromDomain_shouldMapDomainToEntity() {
        // given
        UUID messageId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String content = "Hi there";

        Message domain = Message.restore(
                MessageId.of(messageId.toString()),
                ChatId.of(chatId.toString()),
                UserId.of(userId.toString()),
                content,
                createdAt,
                MessageStatus.READ,
                updatedAt
        );

        ChatEntity chat = new GroupChatEntity(
                chatId,
                "Another Group",
                "ACTIVE",
                createdAt,
                null,
                java.util.Collections.emptyList()
        );

        UserEntity user = UserEntity.builder()
                .id(userId)
                .username("bob")
                .email("bob@example.com")
                .passwordHash("secret")
                .createdAt(createdAt)
                .status("ACTIVE")
                .build();

        // when
        MessageEntity entity = mapper.fromDomain(domain, chat, user);

        // then
        assertThat(entity.getId()).isEqualTo(messageId);
        assertThat(entity.getChat().getId()).isEqualTo(chatId);
        assertThat(entity.getUser().getId()).isEqualTo(userId);
        assertThat(entity.getContent()).isEqualTo(content);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getStatus()).isEqualTo(MessageStatus.READ.name());
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    }
}