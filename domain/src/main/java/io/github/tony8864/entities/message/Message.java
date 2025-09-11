package io.github.tony8864.entities.message;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.exceptions.message.EmptyMessageContentException;
import io.github.tony8864.entities.user.UserId;

import java.time.Instant;

public class Message {
    private final MessageId messageId;
    private final ChatId chatId;
    private final UserId userId;

    private Instant createdAt;
    private String content;
    private MessageStatus status;
    private Instant updatedAt;

    private Message(MessageId messageId, ChatId chatId, UserId userId, String content) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.userId = userId;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = MessageStatus.SENT;
    }

    public static Message create(MessageId messageId, ChatId chatId, UserId userId, String content) {
        if (content == null || content.isBlank()) throw new EmptyMessageContentException();
        return new Message(messageId, chatId, userId, content);
    }

    public static Message restore(
            MessageId messageId,
            ChatId chatId,
            UserId userId,
            String content,
            Instant createdAt,
            MessageStatus status,
            Instant updatedAt
    ) {
        Message message = new Message(messageId, chatId, userId, content);
        message.createdAt = createdAt;
        message.status = status;
        message.updatedAt = updatedAt;
        return message;
    }

    public void markDelivered() {
        if (status != MessageStatus.SENT) throw new IllegalStateException("Message can only be marked DELIVERED from SENT state");
        status = MessageStatus.DELIVERED;
    }

    public void markRead() {
        if (status != MessageStatus.DELIVERED) throw new IllegalStateException("Message can only be marked READ from DELIVERED state");
        status = MessageStatus.READ;
    }

    public void editContent(String newContent) {
        if (newContent == null || newContent.isBlank()) throw new EmptyMessageContentException();
        content = newContent;
        updatedAt = Instant.now();
    }


    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserId getUserId() {
        return userId;
    }

    public ChatId getChatId() {
        return chatId;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
