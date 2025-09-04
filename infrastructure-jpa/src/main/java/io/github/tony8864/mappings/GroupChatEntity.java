package io.github.tony8864.mappings;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_chats")
public class GroupChatEntity {

    @Id
    private UUID id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private MessageEntity lastMessage;
}
