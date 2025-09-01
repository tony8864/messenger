package io.github.tony8864.entities.chat;

import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.exceptions.chat.GroupChatDeletedException;
import io.github.tony8864.exceptions.chat.InvalidGroupException;
import io.github.tony8864.exceptions.chat.UserAlreadyParticipantException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupChatTest {
    private final UserId adminId = UserId.of("admin-1");
    private final UserId memberId = UserId.of("member-1");
    private final UserId strangerId = UserId.of("stranger-1");

    private final Participant admin = Participant.create(adminId, Role.ADMIN);
    private final Participant member = Participant.create(memberId, Role.MEMBER);

    @Test
    void createShouldSucceedWithValidData() {
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-1"),
                new ArrayList<>(List.of(admin, member, Participant.create(UserId.of("m2"), Role.MEMBER))),
                "Team Chat"
        );

        assertNotNull(chat);
    }

    @Test
    void createShouldThrowWhenGroupNameIsInvalid() {
        assertThrows(InvalidGroupException.class,
                () -> GroupChat.create(ChatId.of("chat-2"),
                        new ArrayList<>(List.of(admin, member, Participant.create(UserId.of("m2"), Role.MEMBER))),
                        "   "));
    }

    @Test
    void createShouldThrowWhenNotEnoughParticipants() {
        assertThrows(InvalidGroupException.class,
                () -> GroupChat.create(ChatId.of("chat-3"),
                        new ArrayList<>(List.of(admin, member)),
                        "Tiny Group"));
    }

    @Test
    void createShouldThrowWhenNoAdminPresent() {
        Participant m1 = Participant.create(UserId.of("m1"), Role.MEMBER);
        Participant m2 = Participant.create(UserId.of("m2"), Role.MEMBER);
        Participant m3 = Participant.create(UserId.of("m3"), Role.MEMBER);

        assertThrows(InvalidGroupException.class,
                () -> GroupChat.create(ChatId.of("chat-4"),
                        new ArrayList<>(List.of(m1, m2, m3)),
                        "No Admin Group"));
    }

    @Test
    void addParticipantShouldWorkForAdmin() {
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-5"),
                new ArrayList<>(List.of(admin, member, Participant.create(UserId.of("m2"), Role.MEMBER))),
                "Project"
        );

        Participant newMember = Participant.create(UserId.of("m3"), Role.MEMBER);
        assertDoesNotThrow(() -> chat.addParticipant(adminId, newMember));
    }

    @Test
    void addParticipantShouldThrowIfNotAdmin() {
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-6"),
                new ArrayList<>(List.of(admin, member, Participant.create(UserId.of("m2"), Role.MEMBER))),
                "Project"
        );

        Participant newMember = Participant.create(UserId.of("m3"), Role.MEMBER);
        assertThrows(UnauthorizedOperationException.class,
                () -> chat.addParticipant(memberId, newMember));
    }

    @Test
    void addParticipantShouldThrowIfAlreadyParticipant() {
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-7"),
                new ArrayList<>(List.of(admin, member, Participant.create(UserId.of("m2"), Role.MEMBER))),
                "Group"
        );

        assertThrows(UserAlreadyParticipantException.class,
                () -> chat.addParticipant(adminId, member));
    }

    @Test
    void removeParticipantShouldWorkForAdmin() {
        Participant m2 = Participant.create(UserId.of("m2"), Role.MEMBER);
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-8"),
                new ArrayList<>(List.of(admin, member, m2)),
                "Group"
        );

        assertDoesNotThrow(() -> chat.removeParticipant(adminId, m2.getUserId()));
    }

    @Test
    void removeParticipantShouldThrowIfNotAdmin() {
        Participant m2 = Participant.create(UserId.of("m2"), Role.MEMBER);
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-9"),
                new ArrayList<>(List.of(admin, member, m2)),
                "Group"
        );

        assertThrows(UnauthorizedOperationException.class,
                () -> chat.removeParticipant(memberId, m2.getUserId()));
    }

    @Test
    void removeParticipantShouldThrowIfNoAdminLeft() {
        Participant m2 = Participant.create(UserId.of("m2"), Role.MEMBER);
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-10"),
                new ArrayList<>(List.of(admin, member, m2)),
                "Group"
        );

        // removing admin leaves only members
        assertThrows(InvalidGroupException.class,
                () -> chat.removeParticipant(adminId, admin.getUserId()));
    }

    @Test
    void removeParticipantShouldThrowIfEmpty() {
        Participant m2 = Participant.create(UserId.of("m2"), Role.MEMBER);
        GroupChat chat = GroupChat.create(
                ChatId.of("chat-11"),
                new ArrayList<>(List.of(admin, member, m2)),
                "Group"
        );

        // Remove all non-admins first
        chat.removeParticipant(adminId, member.getUserId());
        chat.removeParticipant(adminId, m2.getUserId());

        // Now only admin remains, removing them leaves no admin
        assertThrows(InvalidGroupException.class,
                () -> chat.removeParticipant(adminId, admin.getUserId()));
    }
}