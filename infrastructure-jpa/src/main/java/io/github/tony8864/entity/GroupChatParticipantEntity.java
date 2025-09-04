package io.github.tony8864.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_chat_participants")
@IdClass(GroupChatParticipantId.class)
public class GroupChatParticipantEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private GroupChatEntity groupChat;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "role", nullable = false)
    private String role;
}
