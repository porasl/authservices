package com.porasl.authservices.connection;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;

public final class FriendMapper {
  private FriendMapper() {}

  // Minimal mapper (no connection metadata)
  public static FriendSummaryDto toDto(User u) {
    if (u == null) return null;
    String fn = u.getFirstname() == null ? "" : u.getFirstname();
    String ln = u.getLastname() == null ? "" : u.getLastname();
    return FriendSummaryDto.builder()
        .id(u.getId())
        .email(u.getEmail())
        .firstname(fn)
        .lastname(ln)
        .profileImageUrl(u.getProfileImageUrl())  // this must exist on User
        .connectionId(null)
        .since(null)
        .build();
  }

  // Overload that includes connection metadata if you have it
  public static FriendSummaryDto toDto(User u, Long connectionId, Long sinceEpochMillis) {
    if (u == null) return null;
    String fn = u.getFirstname() == null ? "" : u.getFirstname();
    String ln = u.getLastname() == null ? "" : u.getLastname();
    return FriendSummaryDto.builder()
        .id(u.getId())
        .email(u.getEmail())
        .firstname(fn)
        .lastname(ln)
        .profileImageUrl(u.getProfileImageUrl())  // this must exist on User
        .connectionId(connectionId)
        .since(sinceEpochMillis)
        .build();
  }
}


