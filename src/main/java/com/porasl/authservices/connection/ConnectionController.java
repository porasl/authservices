// package com.porasl.authservices.connection;

import static com.porasl.authservices.connection.UserConnection.Status.ACCEPTED;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final UserRepository userRepo;
  private final UserConnectionRepository connRepo;

  // ... your existing request(...) methods here ...

  /** Return a de-duplicated list of accepted connections (the *other* user per edge). */
  @Transactional(readOnly = true)
  public List<FriendSummaryDto> listAcceptedConnections(Long me) {
    // edges where I’m requester
    List<UserConnection> a = connRepo.findByUserIdAndStatus(me, ACCEPTED);
    // edges where I’m target
    List<UserConnection> b = connRepo.findByTargetUserIdAndStatus(me, ACCEPTED);

    // preserve recent-first ordering by updatedAt
    Comparator<UserConnection> byUpdatedDesc = Comparator
        .comparing(UserConnection::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
        .reversed();

    List<UserConnection> all = new ArrayList<>(a.size() + b.size());
    all.addAll(a);
    all.addAll(b);
    all.sort(byUpdatedDesc);

    // collect the counterpart user ids in the listed order, de-duplicated
    LinkedHashMap<Long, UserConnection> counterpartEdgeByUserId = new LinkedHashMap<>();
    for (UserConnection uc : all) {
      Long otherId = uc.getUserId().equals(me) ? uc.getTargetUserId() : uc.getUserId();
      counterpartEdgeByUserId.putIfAbsent(otherId, uc);
    }

    if (counterpartEdgeByUserId.isEmpty()) return List.of();

    // fetch user rows in one go
    List<User> users = userRepo.findAllById(counterpartEdgeByUserId.keySet());
    Map<Long, User> byId = users.stream().collect(Collectors.toMap(User::getId, u -> u));

    // map to DTOs in the original order
    List<FriendSummaryDto> out = new ArrayList<>(counterpartEdgeByUserId.size());
    for (Map.Entry<Long, UserConnection> e : counterpartEdgeByUserId.entrySet()) {
      Long otherId = e.getKey();
      UserConnection edge = e.getValue();
      User u = byId.get(otherId);
      if (u == null) continue; // user deleted?
      out.add(FriendSummaryDto.builder()
          .id(u.getId())
          .email(u.getEmail())
          .firstname(u.getFirstname())
          .lastname(u.getLastname())
          .profileImageUrl(u.getProfileImageUrl())
          .connectionId(edge.getId())
          .since(edge.getCreatedAt() == null ? 0L : edge.getCreatedAt().toEpochMilli())
          .build());
    }
    return out;
  }
}
