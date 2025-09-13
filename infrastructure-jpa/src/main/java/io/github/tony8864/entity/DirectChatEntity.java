package io.github.tony8864.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "direct_chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DirectChatEntity extends ChatEntity {

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

    public DirectChatEntity(UUID id, UserEntity user1, UserEntity user2, Instant createdAt, MessageEntity lastMessage) {
        super(id);
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
    }
}
