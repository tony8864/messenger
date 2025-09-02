package io.github.tony8864.entities.chat;

import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.chat.GroupChatDeletedException;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.exceptions.chat.InvalidGroupException;
import io.github.tony8864.exceptions.chat.UserAlreadyParticipantException;
import io.github.tony8864.exceptions.chat.UserNotInChatException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class GroupChat {
    private final ChatId chatId;
    private final List<Participant> participants;
    private final Instant createdAt;

    private String groupName;
    private GroupChatStatus state;
    private MessageId lastMessageId;

    private GroupChat(ChatId chatId, List<Participant> participants, String groupName) {
        this.chatId = chatId;
        this.participants = participants;
        this.groupName = groupName;
        this.createdAt = Instant.now();
        this.state = GroupChatStatus.ACTIVE;
    }

    public static GroupChat create(ChatId chatId, List<Participant> participants, String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new InvalidGroupException("GroupChat cannot have an empty name");
        }
        if (participants == null || participants.size() < 3) {
            throw new InvalidGroupException("GroupChat must be created with at least 3 participants");
        }
        boolean hasAdmin = participants.stream().anyMatch(Participant::isAdmin);
        if (!hasAdmin) {
            throw new InvalidGroupException("GroupChat must have at least one ADMIN");
        }
        return new GroupChat(chatId, participants, groupName);
    }

    public void updateLastMessage(MessageId messageId) {
        this.lastMessageId = messageId;
    }

    public boolean canSendMessage(UserId userId) {
        return participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }

    public void rename(String newGroupName) {
        if (newGroupName == null || newGroupName.isBlank()) {
            throw new InvalidGroupException("GroupChat cannot have an empty name");
        }
        this.groupName = newGroupName;
    }

    public void addParticipant(UserId userId, Participant participant) {
        requireAdmin(userId);
        if (participants.contains(participant)) {
            throw new UserAlreadyParticipantException();
        }
        participants.add(participant);
        updateState();
    }

    public void removeParticipant(UserId requesterId, UserId targetId) {
        requireAdmin(requesterId);

        Participant toRemove = participants.stream()
                .filter(p -> p.getUserId().equals(targetId))
                .findFirst()
                .orElseThrow(() -> new UserNotInChatException(targetId.getValue()));

        participants.remove(toRemove);

        if (!hasAtLeastOneAdmin()) {
            throw new InvalidGroupException("GroupChat must have at least one ADMIN");
        }

        updateState();
    }

    private void requireAdmin(UserId userId) {
        participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .filter(Participant::isAdmin)
                .orElseThrow(() -> new UnauthorizedOperationException("Only admins can manage participants"));
    }

    private boolean hasAtLeastOneAdmin() {
        return participants.stream().anyMatch(Participant::isAdmin);
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

    public ChatId getChatId() {
        return chatId;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public String getGroupName() {
        return groupName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
