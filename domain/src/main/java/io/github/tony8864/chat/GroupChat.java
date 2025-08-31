package io.github.tony8864.chat;

import io.github.tony8864.common.exceptions.EmptyGroupNameException;
import io.github.tony8864.common.exceptions.GroupChatDeletedException;
import io.github.tony8864.message.MessageId;
import io.github.tony8864.user.UserId;

import java.time.Instant;
import java.util.List;

public class GroupChat {
    private final ChatId chatId;
    private final List<UserId> participants;
    private final Instant createdAt;

    private String groupName;
    private GroupChatStatus state;
    private MessageId lastMessageId;

    private GroupChat(ChatId chatId, List<UserId> participants, String groupName) {
        this.chatId = chatId;
        this.participants = participants;
        this.groupName = groupName;
        this.createdAt = Instant.now();
        this.state = GroupChatStatus.ACTIVE;
    }

    public static GroupChat create(ChatId chatId, List<UserId> participants, String groupName) {
        if (groupName == null || groupName.isBlank()) throw new EmptyGroupNameException();
        return new GroupChat(chatId, participants, groupName);
    }

    public void addParticipant(UserId userId) {
        participants.add(userId);
        updateState();
    }

    public void removeParticipant(UserId userId) {
        participants.remove(userId);
        updateState();
    }

    private void updateState() {
        if (participants.isEmpty()) {
            throw new GroupChatDeletedException();
        }
        else if (participants.size() == 1) {
            state = GroupChatStatus.DEGRADED;
        }
        else {
            state = GroupChatStatus.ACTIVE;
        }
    }
}
