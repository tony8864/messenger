package io.github.tony8864.chat.common.dto;

import io.github.tony8864.entities.participant.Participant;
import io.github.tony8864.entities.participant.Role;

public record ParticipantDto(
        String userId,
        Role role
) {
    public static ParticipantDto fromDomain(Participant participant) {
        return new ParticipantDto(
                participant.getUserId().getValue(),
                participant.getRole()
        );
    }
}
