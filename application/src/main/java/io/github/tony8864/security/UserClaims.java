package io.github.tony8864.security;

import io.github.tony8864.entities.participant.Role;

import java.util.Set;

public record UserClaims(
        String userId,
        String email,
        Set<Role> roles
) {
}
