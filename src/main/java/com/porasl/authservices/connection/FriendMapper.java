package com.porasl.authservices.connection;

import com.porasl.authservices.user.User;
import com.porasl.common.dto.FriendSummaryDto;

public final class FriendMapper {
  private FriendMapper() {}

  public static FriendSummaryDto toDto(User u) {
    if (u == null) return null;

    String fn = u.getFirstname() == null ? "" : u.getFirstname();
    String ln = u.getLastname() == null ? "" : u.getLastname();

    return new FriendSummaryDto(
        null,                       // connectionId
        u.getEmail(),
        fn,
        ln,
        u.getProfileImageUrl(),
        u.getCreatedDate(),         // since
        null,                       // notes
        0L,                         // requesterId
        u.getId()                   // targetId
    );
  }

  public static FriendSummaryDto toDto(User u, Long connectionId, Long sinceEpochMillis) {
    if (u == null) return null;

    String fn = u.getFirstname() == null ? "" : u.getFirstname();
    String ln = u.getLastname() == null ? "" : u.getLastname();

    return new FriendSummaryDto(
        connectionId,
        u.getEmail(),
        fn,
        ln,
        u.getProfileImageUrl(),
        sinceEpochMillis,
        null,
        u.getId(),                  // requesterId
        u.getId()                   // targetId (adjust if different)
    );
  }
}
