package io.github.tony8864.exceptions.chat;

import io.github.tony8864.exceptions.DomainException;

public class UserNotInChatException extends DomainException {
    public UserNotInChatException(String id) {
        super("User with id \"" + id + "\" is not in group chat");
    }
}
