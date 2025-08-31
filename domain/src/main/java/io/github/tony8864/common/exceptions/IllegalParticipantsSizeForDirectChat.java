package io.github.tony8864.common.exceptions;

public class IllegalParticipantsSizeForDirectChat extends DomainException {
    public IllegalParticipantsSizeForDirectChat() {
        super("Direct chat can contain only 2 participants");
    }
}
