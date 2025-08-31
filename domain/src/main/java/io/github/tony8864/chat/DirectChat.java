package io.github.tony8864.chat;

import io.github.tony8864.common.exceptions.IllegalParticipantsSizeForDirectChat;
import io.github.tony8864.message.MessageId;
import io.github.tony8864.user.UserId;

import java.time.Instant;
import java.util.List;

public class DirectChat {
    private final ChatId chatId;
    private final List<UserId> participants;
    private final Instant createdAt;
    private MessageId lastMessageId;

    private DirectChat(ChatId chatId, List<UserId> participants) {
        this.chatId = chatId;
        this.participants = participants;
        this.createdAt = Instant.now();
    }

    public static DirectChat create(ChatId chatId, List<UserId> participants) {
        if (participants.size() != 2) throw new IllegalParticipantsSizeForDirectChat();
        return new DirectChat(chatId, participants);
    }

    public void updateLastMessage(MessageId messageId) {
        this.lastMessageId = messageId;
    }

    public boolean canSendMessage(UserId userId) {
        return participants.contains(userId);
    }

    public List<UserId> getParticipants() {
        return participants;
    }

    public ChatId getChatId() {
        return chatId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageId getLastMessageId() {
        return lastMessageId;
    }
}
