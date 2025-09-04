package io.github.tony8864.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupChatParticipantId implements Serializable {
    private UUID groupChat;
    private UUID user;
}
