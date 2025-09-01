package io.github.tony8864.usecases.user.register.dto;

import io.github.tony8864.entities.user.User;

public record RegisterUserResponse(String userId, String email, String username) {
    public static RegisterUserResponse from(User user) {
        return new RegisterUserResponse(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getUsername()
        );
    }
}
