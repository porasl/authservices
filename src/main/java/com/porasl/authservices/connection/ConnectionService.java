package com.porasl.authservices.connection;

import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
public class ConnectionService {

private final UserRepository users;
private final UserConnectionRepository connections;

public ConnectionService(UserRepository users, UserConnectionRepository connections) {
 this.users = users;
 this.connections = connections;
}

private record Pair(User a, User b) {}

/** Ensure a.id < b.id for unique pair */
private Pair canonical(User u1, User u2) {
 return (u1.getId() < u2.getId()) ? new Pair(u1, u2) : new Pair(u2, u1);
}

@Transactional
public User addAcceptedConnection(long userId, long targetUserId) {
 if (userId == targetUserId) {
   throw new IllegalArgumentException("Cannot connect a user to themselves");
 }
 User u1 = users.findById(userId).orElseThrow();
 User u2 = users.findById(targetUserId).orElseThrow();
 Pair p = canonical(u1, u2);

 UserConnection row = connections.findByUserAIdAndUserBId(p.a().getId(), p.b().getId())
   .orElseGet(() -> UserConnection.builder().userA(p.a()).userB(p.b()).build());

 row.setStatus(ConnectionStatus.ACCEPTED);
 connections.save(row);
 return u2; // return the "other side" for convenience
}

@Transactional(readOnly = true)
public List<User> listAcceptedConnections(long userId) {
 List<User> asA = connections.findAcceptedWhenUserIsA(userId);
 List<User> asB = connections.findAcceptedWhenUserIsB(userId);
 // Merge + distinct by id
 return Stream.concat(asA.stream(), asB.stream())
     .collect(() -> new LinkedHashMap<Long, User>(),
              (m, u) -> m.putIfAbsent(u.getId(), u),
              Map::putAll)
     .values().stream().toList();
}
}

