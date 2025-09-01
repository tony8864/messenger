package io.github.tony8864.exceptions.chat;

import io.github.tony8864.exceptions.DomainException;

public class IllegalParticipantsSizeForDirectChat extends DomainException {
    public IllegalParticipantsSizeForDirectChat() {
        super("Direct chat can contain only 2 participants");
    }
}
