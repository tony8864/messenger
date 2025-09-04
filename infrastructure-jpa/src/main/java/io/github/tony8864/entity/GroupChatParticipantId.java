package io.github.tony8864.entity;

import java.io.Serializable;
import java.util.UUID;

public class GroupChatParticipantId implements Serializable {
    private UUID groupChat;
    private UUID user;
}
