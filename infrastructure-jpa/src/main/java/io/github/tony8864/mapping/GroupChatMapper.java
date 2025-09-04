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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class GroupChatMapper {
    public GroupChat toDomain(GroupChatEntity entity) {
        List<Participant> participants = entity.getParticipants().stream()
                .map(p -> Participant.create(
                        UserId.of(p.getUser().getId().toString()), Role.valueOf(p.getRole())
                )).toList();

        GroupChat chat = GroupChat.create(
                ChatId.of(entity.getId().toString()),
                participants,
                entity.getGroupName()
        );

        if (entity.getLastMessage() != null) {
            chat.updateLastMessage(MessageId.of(entity.getLastMessage().getId().toString()));
        }

        return chat;
    }

    public GroupChatEntity fromDomain(GroupChat chat, List<UserEntity> users, MessageEntity lastMessage) {
        GroupChatEntity entity = new GroupChatEntity(
                UUID.fromString(chat.getChatId().getValue()),
                chat.getGroupName(),
                chat.getState().name(),
                chat.getCreatedAt(),
                lastMessage,
                new ArrayList<>()
        );

        List<GroupChatParticipantEntity> participantEntities = chat.getParticipants().stream()
                .map(p -> {
                    UserEntity userEntity = users.stream()
                            .filter(u -> u.getId().toString().equals(p.getUserId().getValue()))
                            .findFirst()
                            .orElseThrow();
                    return new GroupChatParticipantEntity(entity, userEntity, p.getRole().name());
                })
                .toList();

        entity.getParticipants().addAll(participantEntities);

        return entity;
    }

}
