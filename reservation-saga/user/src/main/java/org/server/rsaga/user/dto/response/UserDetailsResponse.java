package org.server.rsaga.user.dto.response;

import org.server.rsaga.user.domain.User;

public record UserDetailsResponse(
        Long userId,
        String name
) {
    public static UserDetailsResponse of(User user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getName()
        );
    }
}
