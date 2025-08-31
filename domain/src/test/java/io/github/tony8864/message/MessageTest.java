package io.github.tony8864.message;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.exceptions.EmptyMessageContentException;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.message.MessageStatus;
import io.github.tony8864.entities.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private final MessageId messageId = MessageId.of("msg-1");
    private final ChatId chatId = ChatId.of("chat-1");
    private final UserId userId = UserId.of("user-1");

    @Test
    void createShouldSucceedWithValidContent() {
        Message msg = Message.create(messageId, chatId, userId, "Hello!");
        assertNotNull(msg);
    }

    @Test
    void createShouldThrowWhenContentIsNull() {
        assertThrows(EmptyMessageContentException.class,
                () -> Message.create(messageId, chatId, userId, null));
    }

    @Test
    void createShouldThrowWhenContentIsBlank() {
        assertThrows(EmptyMessageContentException.class,
                () -> Message.create(messageId, chatId, userId, "   "));
    }

    @Test
    void markDeliveredShouldChangeStatusFromSent() throws Exception {
        Message msg = Message.create(messageId, chatId, userId, "Hi");

        msg.markDelivered();

        var statusField = Message.class.getDeclaredField("status");
        statusField.setAccessible(true);
        assertEquals(MessageStatus.DELIVERED, statusField.get(msg));
    }

    @Test
    void markDeliveredShouldThrowIfNotSent() {
        Message msg = Message.create(messageId, chatId, userId, "Hi");
        msg.markDelivered();

        assertThrows(IllegalStateException.class, msg::markDelivered);
    }

    @Test
    void markReadShouldChangeStatusFromDelivered() throws Exception {
        Message msg = Message.create(messageId, chatId, userId, "Hi");
        msg.markDelivered();

        msg.markRead();

        var statusField = Message.class.getDeclaredField("status");
        statusField.setAccessible(true);
        assertEquals(MessageStatus.READ, statusField.get(msg));
    }

    @Test
    void markReadShouldThrowIfNotDelivered() {
        Message msg = Message.create(messageId, chatId, userId, "Hi");
        assertThrows(IllegalStateException.class, msg::markRead);
    }

    @Test
    void editContentShouldUpdateContentAndUpdatedAt() throws Exception {
        Message msg = Message.create(messageId, chatId, userId, "Old content");
        Instant before = Instant.now();

        msg.editContent("New content");

        var contentField = Message.class.getDeclaredField("content");
        contentField.setAccessible(true);
        assertEquals("New content", contentField.get(msg));

        var updatedAtField = Message.class.getDeclaredField("updatedAt");
        updatedAtField.setAccessible(true);
        Instant updatedAt = (Instant) updatedAtField.get(msg);

        assertNotNull(updatedAt);
        assertTrue(!updatedAt.isBefore(before),
                "updatedAt should be equal to or after the time before editContent()");

    }

    @Test
    void editContentShouldThrowWhenBlankOrNull() {
        Message msg = Message.create(messageId, chatId, userId, "Valid content");

        assertThrows(EmptyMessageContentException.class, () -> msg.editContent(null));
        assertThrows(EmptyMessageContentException.class, () -> msg.editContent("   "));
    }
}