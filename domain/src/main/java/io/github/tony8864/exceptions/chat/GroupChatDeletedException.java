package io.github.tony8864.exceptions.chat;

import io.github.tony8864.exceptions.DomainException;

public class GroupChatDeletedException extends DomainException {
    public GroupChatDeletedException() {
        super("Group cannot exist without participants");
    }
}
