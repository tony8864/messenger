package io.github.tony8864.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatEntity extends ChatEntity {

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private MessageEntity lastMessage;

    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupChatParticipantEntity> participants = new ArrayList<>();

    public GroupChatEntity(
            UUID id,
            String groupName,
            String state,
            Instant createdAt,
            MessageEntity lastMessage,
            List<GroupChatParticipantEntity> participants
    ) {
        super(id);
        this.groupName = groupName;
        this.state = state;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.participants = participants;
    }
}
