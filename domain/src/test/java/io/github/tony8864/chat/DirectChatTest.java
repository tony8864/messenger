package io.github.tony8864.chat;

import io.github.tony8864.common.exceptions.IllegalParticipantsSizeForDirectChat;
import io.github.tony8864.message.MessageId;
import io.github.tony8864.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectChatTest {
    private final UserId user1 = UserId.of("11111111-1111-1111-1111-111111111111");
    private final UserId user2 = UserId.of("22222222-2222-2222-2222-222222222222");
    private final UserId stranger = UserId.of("33333333-3333-3333-3333-333333333333");

    @Test
    void createShouldSucceedWithTwoParticipants() {
        ChatId chatId = ChatId.of("chat-1");
        DirectChat chat = DirectChat.create(chatId, List.of(user1, user2));

        assertNotNull(chat);
        assertEquals(chatId, chat.getChatId());
        assertEquals(List.of(user1, user2), chat.getParticipants());
        assertNotNull(chat.getCreatedAt());
        assertNull(chat.getLastMessageId());
    }

    @Test
    void createShouldThrowExceptionIfLessThanTwoParticipants() {
        ChatId chatId = ChatId.of("chat-2");
        assertThrows(IllegalParticipantsSizeForDirectChat.class,
                () -> DirectChat.create(chatId, List.of(user1)));
    }

    @Test
    void createShouldThrowExceptionIfMoreThanTwoParticipants() {
        ChatId chatId = ChatId.of("chat-3");
        assertThrows(IllegalParticipantsSizeForDirectChat.class,
                () -> DirectChat.create(chatId, List.of(user1, user2, stranger)));
    }

    @Test
    void updateLastMessageShouldChangeLastMessageId() {
        DirectChat chat = DirectChat.create(ChatId.of("chat-4"), List.of(user1, user2));
        MessageId msg = MessageId.of("msg-1");

        chat.updateLastMessage(msg);

        assertEquals(msg, chat.getLastMessageId());
    }

    @Test
    void canSendMessageShouldReturnTrueForParticipants() {
        DirectChat chat = DirectChat.create(ChatId.of("chat-5"), List.of(user1, user2));

        assertTrue(chat.canSendMessage(user1));
        assertTrue(chat.canSendMessage(user2));
    }

    @Test
    void canSendMessageShouldReturnFalseForNonParticipant() {
        DirectChat chat = DirectChat.create(ChatId.of("chat-6"), List.of(user1, user2));

        assertFalse(chat.canSendMessage(stranger));
    }

    @Test
    void createdAtShouldBeCloseToNow() {
        Instant before = Instant.now();
        DirectChat chat = DirectChat.create(ChatId.of("chat-7"), List.of(user1, user2));
        Instant after = Instant.now();

        assertTrue(!chat.getCreatedAt().isBefore(before) && !chat.getCreatedAt().isAfter(after));
    }
}