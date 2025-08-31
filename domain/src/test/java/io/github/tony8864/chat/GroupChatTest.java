package io.github.tony8864.chat;

import io.github.tony8864.exceptions.EmptyGroupNameException;
import io.github.tony8864.exceptions.GroupChatDeletedException;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.chat.GroupChatStatus;
import io.github.tony8864.entities.user.UserId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupChatTest {
    private final UserId user1 = UserId.of("11111111-1111-1111-1111-111111111111");
    private final UserId user2 = UserId.of("22222222-2222-2222-2222-222222222222");
    private final UserId user3 = UserId.of("33333333-3333-3333-3333-333333333333");

    @Test
    void createShouldSucceedWithValidName() {
        GroupChat chat = GroupChat.create(ChatId.of("chat-1"),
                new ArrayList<>(List.of(user1, user2)),
                "Study Group");

        assertNotNull(chat);
    }

    @Test
    void createShouldThrowExceptionWhenNameIsNull() {
        assertThrows(EmptyGroupNameException.class,
                () -> GroupChat.create(ChatId.of("chat-2"),
                        new ArrayList<>(List.of(user1, user2)),
                        null));
    }

    @Test
    void createShouldThrowExceptionWhenNameIsBlank() {
        assertThrows(EmptyGroupNameException.class,
                () -> GroupChat.create(ChatId.of("chat-3"),
                        new ArrayList<>(List.of(user1, user2)),
                        "   "));
    }

    @Test
    void addParticipantShouldKeepStateActive() {
        GroupChat chat = GroupChat.create(ChatId.of("chat-4"),
                new ArrayList<>(List.of(user1, user2)),
                "Friends");

        chat.addParticipant(user3);

        // state should remain ACTIVE with >= 2 participants
        assertDoesNotThrow(() -> {
            var stateField = GroupChat.class.getDeclaredField("state");
            stateField.setAccessible(true);
            GroupChatStatus state = (GroupChatStatus) stateField.get(chat);
            assertEquals(GroupChatStatus.ACTIVE, state);
        });
    }

    @Test
    void removeParticipantShouldSetStateToDegradedWhenOneLeft() {
        GroupChat chat = GroupChat.create(ChatId.of("chat-5"),
                new ArrayList<>(List.of(user1, user2)),
                "Work");

        chat.removeParticipant(user1);

        assertDoesNotThrow(() -> {
            var stateField = GroupChat.class.getDeclaredField("state");
            stateField.setAccessible(true);
            GroupChatStatus state = (GroupChatStatus) stateField.get(chat);
            assertEquals(GroupChatStatus.DEGRADED, state);
        });
    }

    @Test
    void removeParticipantShouldThrowWhenNoParticipantsLeft() {
        GroupChat chat = GroupChat.create(ChatId.of("chat-6"),
                new ArrayList<>(List.of(user1)),
                "Solo");

        assertThrows(GroupChatDeletedException.class,
                () -> chat.removeParticipant(user1));
    }
}