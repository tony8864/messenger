package io.github.tony8864.chat.common.exception;

public class GroupChatNotFoundException extends RuntimeException {
    public GroupChatNotFoundException(String id) {
        super("Group chat with id \"" + id + "\" not found");
    }
}
