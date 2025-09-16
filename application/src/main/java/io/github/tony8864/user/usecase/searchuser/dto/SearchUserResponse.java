package io.github.tony8864.user.usecase.searchuser.dto;

import io.github.tony8864.entities.user.User;

public record SearchUserResponse(
        String userId,
        String username
) {
    public static SearchUserResponse fromDomain(User user) {
        return new SearchUserResponse(user.getUserId().getValue(), user.getUsername());
    }
}
