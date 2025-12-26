package com.porasl.authservices.connection;

import com.porasl.authservices.user.User;
import com.porasl.common.dto.FriendSummaryDto;

import java.util.Objects;

public final class FriendMapper {

    private FriendMapper() {}

    public static FriendSummaryDto toDto(
            UserConnection connection,
            User currentUser
    ) {
        if (connection == null || currentUser == null) {
            return null;
        }

        User requester = connection.getRequester();
        User target = connection.getTarget();

        // Determine the "other" user safely
        User other = Objects.equals(requester.getId(), currentUser.getId())
                ? target
                : requester;

        return new FriendSummaryDto(
                connection.getId(),                         // connectionId ✅
                other.getEmail(),
                other.getFirstname(),
                other.getLastname(),
                other.getProfileImageUrl(),
                connection.getCreatedAt() != null
                        ? connection.getCreatedAt().toEpochMilli()
                        : null,                              // since ✅
                connection.getNote(),
                requester.getId(),                          // requesterId ✅
                target.getId()                              // targetId ✅
        );
    }
}
