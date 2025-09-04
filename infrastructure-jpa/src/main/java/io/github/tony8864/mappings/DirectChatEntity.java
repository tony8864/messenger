package io.github.tony8864.mappings;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.user.UserId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "direct_chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatEntity extends ChatEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private UserEntity user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private UserEntity user2;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private MessageEntity lastMessage;

    @Builder
    public DirectChatEntity(
            UUID id,
            UserEntity user1,
            UserEntity user2,
            Instant createdAt,
            MessageEntity lastMessage
    ) {
        super(id); // sets id in ChatEntity
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
    }

    public DirectChat toDomain() {
        List<UserId> participants = List.of(
                UserId.of(user1.getId().toString()),
                UserId.of(user2.getId().toString())
        );

        DirectChat chat = DirectChat.create(
                ChatId.of(getId().toString()),
                participants
        );

        if (lastMessage != null) {
            chat.updateLastMessage(MessageId.of(lastMessage.getId().toString()));
        }

        return chat;
    }

    public static DirectChatEntity fromDomain(DirectChat chat, UserEntity user1, UserEntity user2, MessageEntity lastMessage) {
        return DirectChatEntity.builder()
                .id(UUID.fromString(chat.getChatId().getValue()))
                .user1(user1)
                .user2(user2)
                .createdAt(chat.getCreatedAt())
                .lastMessage(lastMessage)
                .build();
    }
}
