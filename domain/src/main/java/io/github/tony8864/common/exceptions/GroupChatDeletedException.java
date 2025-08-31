package io.github.tony8864.common.exceptions;

public class GroupChatDeletedException extends DomainException {
    public GroupChatDeletedException() {
        super("Group cannot exist without participants");
    }
}
