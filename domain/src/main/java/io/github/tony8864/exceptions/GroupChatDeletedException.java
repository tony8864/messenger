package io.github.tony8864.exceptions;

public class GroupChatDeletedException extends DomainException {
    public GroupChatDeletedException() {
        super("Group cannot exist without participants");
    }
}
