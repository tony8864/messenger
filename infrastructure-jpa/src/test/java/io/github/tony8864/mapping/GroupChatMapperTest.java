package io.github.tony8864.mapping;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.GroupChatEntity;
import io.github.tony8864.entity.GroupChatParticipantEntity;
import io.github.tony8864.entity.MessageEntity;
import io.github.tony8864.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupChatMapperTest {
    private final GroupChatMapper mapper = new GroupChatMapper();

    @Test
    void toDomain_shouldMapEntityWithoutLastMessage() {
        // given
        UUID chatId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        UserEntity user1 = new UserEntity(user1Id, "alice", "alice@example.com", "hash1", "ONLINE", Instant.now());
        UserEntity user2 = new UserEntity(user2Id, "bob", "bob@example.com", "hash2", "OFFLINE", Instant.now());
        UserEntity user3 = new UserEntity(user3Id, "carol", "carol@example.com", "hash3", "AWAY", Instant.now());

        GroupChatEntity groupChatEntity = new GroupChatEntity(
                chatId,
                "dev-team",
                "ACTIVE",
                Instant.now(),
                null, // no lastMessage
                List.of(
                        new GroupChatParticipantEntity(null, user1, Role.ADMIN.name()),
                        new GroupChatParticipantEntity(null, user2, Role.MEMBER.name()),
                        new GroupChatParticipantEntity(null, user3, Role.MEMBER.name())
                )
        );

        // when
        GroupChat domain = mapper.toDomain(groupChatEntity);

        // then
        assertEquals(chatId.toString(), domain.getChatId().getValue());
        assertEquals("dev-team", domain.getGroupName());
        assertEquals(3, domain.getParticipants().size());
        assertNull(domain.getLastMessageId());
    }

    @Test
    void toDomain_shouldMapEntityWithLastMessage() {
        // given
        UUID chatId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        UserEntity user1 = new UserEntity(user1Id, "alice", "alice@example.com", "hash1", "ONLINE", Instant.now());
        UserEntity user2 = new UserEntity(user2Id, "bob", "bob@example.com", "hash2", "OFFLINE", Instant.now());
        UserEntity user3 = new UserEntity(user3Id, "carol", "carol@example.com", "hash3", "AWAY", Instant.now());

        MessageEntity message = new MessageEntity(messageId, null, user1, Instant.now(), "hello", "SENT", null);

        GroupChatEntity groupChatEntity = new GroupChatEntity(
                chatId,
                "qa-team",
                "ACTIVE",
                Instant.now(),
                message,
                List.of(
                        new GroupChatParticipantEntity(null, user1, Role.ADMIN.name()),
                        new GroupChatParticipantEntity(null, user2, Role.MEMBER.name()),
                        new GroupChatParticipantEntity(null, user3, Role.MEMBER.name())
                )
        );

        // when
        GroupChat domain = mapper.toDomain(groupChatEntity);

        // then
        assertEquals(chatId.toString(), domain.getChatId().getValue());
        assertEquals("qa-team", domain.getGroupName());
        assertEquals(3, domain.getParticipants().size());
        assertEquals(messageId.toString(), domain.getLastMessageId().getValue());
    }

    @Test
    void fromDomain_shouldMapDomainToEntity() {
        // given
        ChatId chatId = ChatId.of(UUID.randomUUID().toString());
        UserId user1Id = UserId.of(UUID.randomUUID().toString());
        UserId user2Id = UserId.of(UUID.randomUUID().toString());
        UserId user3Id = UserId.of(UUID.randomUUID().toString());

        Participant p1 = Participant.create(user1Id, Role.ADMIN);
        Participant p2 = Participant.create(user2Id, Role.MEMBER);
        Participant p3 = Participant.create(user3Id, Role.MEMBER);

        GroupChat domain = GroupChat.create(chatId, List.of(p1, p2, p3), "devs");
        MessageId lastMessageId = MessageId.of(UUID.randomUUID().toString());
        domain.updateLastMessage(lastMessageId);

        // matching UserEntities
        UserEntity user1 = new UserEntity(UUID.fromString(user1Id.getValue()), "alice", "alice@example.com", "hash1", "ONLINE", Instant.now());
        UserEntity user2 = new UserEntity(UUID.fromString(user2Id.getValue()), "bob", "bob@example.com", "hash2", "OFFLINE", Instant.now());
        UserEntity user3 = new UserEntity(UUID.fromString(user3Id.getValue()), "carol", "carol@example.com", "hash3", "AWAY", Instant.now());

        MessageEntity lastMessage = new MessageEntity(UUID.fromString(lastMessageId.getValue()), null, user1, Instant.now(), "hi", "SENT", null);

        // when
        GroupChatEntity entity = mapper.fromDomain(domain, List.of(user1, user2, user3), lastMessage);

        // then
        assertEquals(UUID.fromString(chatId.getValue()), entity.getId());
        assertEquals("devs", entity.getGroupName());
        assertEquals(domain.getState().name(), entity.getState());
        assertEquals(lastMessage.getId(), entity.getLastMessage().getId());

        // participants
        assertEquals(3, entity.getParticipants().size());
        assertTrue(entity.getParticipants().stream()
                .anyMatch(pe -> pe.getUser().getId().equals(user1.getId()) && pe.getRole().equals(Role.ADMIN.name())));
        assertTrue(entity.getParticipants().stream()
                .anyMatch(pe -> pe.getUser().getId().equals(user2.getId()) && pe.getRole().equals(Role.MEMBER.name())));
        assertTrue(entity.getParticipants().stream()
                .anyMatch(pe -> pe.getUser().getId().equals(user3.getId()) && pe.getRole().equals(Role.MEMBER.name())));

        // all participants should reference back to the group chat entity
        entity.getParticipants().forEach(pe -> assertSame(entity, pe.getGroupChat()));
    }
}