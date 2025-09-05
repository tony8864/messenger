package io.github.tony8864.security;

import io.github.tony8864.entities.participant.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record UserClaims(
        String userId,
        String email,
        Set<String> roles
) {
    public static UserClaims fromDomain(String userId, String email, Set<Role> domainRoles) {
        return new UserClaims(
                userId,
                email,
                domainRoles.stream().map(Role::name).collect(Collectors.toSet())
        );
    }

    public Set<Role> toDomainRoles() {
        return roles.stream().map(Role::valueOf).collect(Collectors.toSet());
    }
}
