package com.porasl.authservices.connection;

import static com.porasl.authservices.connection.UserConnection.Status.ACCEPTED;
import static com.porasl.authservices.connection.UserConnection.Status.PENDING;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final UserRepository userRepo;
  private final UserConnectionRepository connRepo;

  /** Create (or return existing) connection request by target email. */
  @Transactional
  public UserConnection requestByEmail(Long requesterId, String targetEmail) {
    if (targetEmail == null || targetEmail.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
    }

    User requester = userRepo.findById(requesterId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));

    User target = userRepo.findByEmailIgnoreCase(targetEmail.trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with that email"));

    if (Objects.equals(requester.getId(), target.getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect to yourself");
    }

    // Check if any edge already exists (either direction)
    Optional<UserConnection> existing = connRepo.findBetweenUsers(requester.getId(), target.getId());
    if (existing.isPresent()) {
      return existing.get(); // return existing edge (PENDING/ACCEPTED/etc.)
    }

    // Create a new pending request (requester -> target)
    UserConnection uc = new UserConnection();
    uc.setUserId(requester.getId());
    uc.setTargetUserId(target.getId());
    uc.setStatus(PENDING);
    uc.touchCreated(); // if you have helper to set createdAt/updatedAt; otherwise set explicitly
    return connRepo.save(uc);
  }

  /** Return a de-duplicated, recent-first list of accepted connections (the other user per edge). */
  @Transactional(readOnly = true)
  public List<FriendSummaryDto> listAcceptedConnections(Long me) {
    // edges where I’m requester
    List<UserConnection> a = connRepo.findByUserIdAndStatus(me, ACCEPTED);
    // edges where I’m target
    List<UserConnection> b = connRepo.findByTargetUserIdAndStatus(me, ACCEPTED);

    Comparator<UserConnection> byUpdatedDesc = Comparator
        .comparing(UserConnection::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
        .reversed();

    List<UserConnection> all = new ArrayList<>(a.size() + b.size());
    all.addAll(a);
    all.addAll(b);
    all.sort(byUpdatedDesc);

    // counterpart user ids, keeping first-seen (most recent) order
    LinkedHashMap<Long, UserConnection> counterpartEdgeByUserId = new LinkedHashMap<>();
    for (UserConnection uc : all) {
      Long otherId = uc.getUserId().equals(me) ? uc.getTargetUserId() : uc.getUserId();
      counterpartEdgeByUserId.putIfAbsent(otherId, uc);
    }
    if (counterpartEdgeByUserId.isEmpty()) return List.of();

    // fetch users in one round-trip
    List<User> users = userRepo.findAllById(counterpartEdgeByUserId.keySet());
    Map<Long, User> byId = users.stream().collect(Collectors.toMap(User::getId, u -> u));

    // map to DTOs in the original (recent-first) order
    List<FriendSummaryDto> out = new ArrayList<>(counterpartEdgeByUserId.size());
    for (Map.Entry<Long, UserConnection> e : counterpartEdgeByUserId.entrySet()) {
      Long otherId = e.getKey();
      UserConnection edge = e.getValue();
      User u = byId.get(otherId);
      if (u == null) continue; // user deleted?
      out.add(new FriendSummaryDto(
          u.getId(),
          u.getEmail(),
          u.getFirstname(),
          u.getLastname(),
          u.getProfileImageUrl(),
          edge.getId(),
          edge.getCreatedAt() == null ? 0L : edge.getCreatedAt().toEpochMilli()
      ));
    }
    return out;
  }

  /** Accept a pending connection where current user is the target. */
  @Transactional
  public UserConnection accept(Long me, Long connectionId) {
    UserConnection uc = connRepo.findById(connectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));
    if (!Objects.equals(uc.getTargetUserId(), me)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the target user can accept");
    }
    uc.setStatus(ACCEPTED);
    uc.touchUpdated();
    return connRepo.save(uc);
  }

  /** Decline (or cancel) a pending connection; me can be requester (cancel) or target (decline). */
  @Transactional
  public void delete(Long me, Long connectionId) {
    UserConnection uc = connRepo.findById(connectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));
    if (!Objects.equals(uc.getUserId(), me) && !Objects.equals(uc.getTargetUserId(), me)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this connection");
    }
    connRepo.deleteById(connectionId);
  }
}
