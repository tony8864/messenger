package io.github.tony8864.user.dto;

public record LogoutApiResponse(String message) {
    public static LogoutApiResponse success(String userId) {
        return new LogoutApiResponse("User " + userId + " logged out successfully.");
    }
}
