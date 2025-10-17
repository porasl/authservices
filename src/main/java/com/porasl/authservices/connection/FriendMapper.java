package com.porasl.authservices.connection;

import com.porasl.authservices.dto.FriendSummaryDto;

//com.porasl.authservices.connection.FriendMapper
public final class FriendMapper {
private FriendMapper() {}
public static FriendSummaryDto toDto(com.porasl.authservices.user.User u) {
 if (u == null) return null;
 String fn = u.getFirstname() == null ? "" : u.getFirstname();
 String ln = u.getLastname() == null ? "" : u.getLastname();
 String name = (fn + " " + ln).trim();
 return new FriendSummaryDto(
     u.getId(),
     name.isEmpty() ? u.getEmail() : name,
     u.getEmail(),
     u.getProfileImageUrl()
 );
}
}
