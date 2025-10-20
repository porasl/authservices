package com.porasl.authservices.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.FriendMapper;
import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.connection.UserConnectionRepository;
import com.porasl.authservices.dto.ConnectionDto;
import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final UserRepository userRepo;
  private final UserConnectionRepository connRepo;

//com.porasl.authservices.service.ConnectionService


  /** Return accepted connections as FriendSummaryDto (never null). */
  public List<FriendSummaryDto> listAcceptedConnections(Long userId) {
    List<User> friends = connRepo.findAcceptedCounterparties(userId, UserConnection.Status.ACCEPTED);
    if (friends == null || friends.isEmpty()) return Collections.emptyList();
    return friends.stream().map(FriendMapper::toDto).toList();
  }

  /** Create a connection request (or accept if reverse pending exists). */
  @Transactional
  public ConnectionDto request(Long requesterId, Long targetId) {
    if (requesterId.equals(targetId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
    }

    userRepo.findById(targetId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "target not found"));

    // Already exists in this direction?
    var existing = connRepo.findByUserIdAndTargetUserId(requesterId, targetId);
    if (existing.isPresent()) return ConnectionDto.of(existing.get(), /*created=*/false);

    // Reverse pending? -> accept both
    var reversePending = connRepo.findByUserIdAndTargetUserIdAndStatus(
        targetId, requesterId, UserConnection.Status.PENDING);
    if (reversePending.isPresent()) {
      var a = reversePending.get();
      a.setStatus(UserConnection.Status.ACCEPTED);
      a.setUpdatedAt(Instant.now());
      connRepo.save(a);

      var b = new UserConnection();
      b.setUserId(requesterId);
      b.setTargetUserId(targetId);
      b.setStatus(UserConnection.Status.ACCEPTED);
      b.setCreatedBy(requesterId);
      b.setCreatedAt(Instant.now());
      b.setUpdatedAt(Instant.now());
      var saved = connRepo.save(b);

      return ConnectionDto.of(saved, /*created=*/true);
    }

    // Create new pending edge
    var edge = new UserConnection();
    edge.setUserId(requesterId);
    edge.setTargetUserId(targetId);
    edge.setStatus(UserConnection.Status.PENDING);
    edge.setCreatedBy(requesterId);
    edge.setCreatedAt(Instant.now());
    edge.setUpdatedAt(Instant.now());
    var saved = connRepo.save(edge);

    return ConnectionDto.of(saved, /*created=*/true);
  }

  /** Create/request by target email. Returns the UserConnection entity. */
  @Transactional
  public UserConnection requestByEmail(long requesterId, String targetEmail) {
    if (targetEmail == null || targetEmail.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
    }
    User target = userRepo.findByEmailIgnoreCase(targetEmail.trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "target user not found"));

    if (requesterId ==target.getId()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
    }

    var existing = connRepo.findByUserIdAndTargetUserId(requesterId, target.getId());
    if (existing.isPresent()) return existing.get();

    // Reverse pending? -> accept both
    var reversePending = connRepo.findByUserIdAndTargetUserIdAndStatus(
        target.getId(), requesterId, UserConnection.Status.PENDING);
    if (reversePending.isPresent()) {
      var a = reversePending.get();
      a.setStatus(UserConnection.Status.ACCEPTED);
      a.setUpdatedAt(Instant.now());
      connRepo.save(a);

      var b = new UserConnection();
      b.setUserId(requesterId);
      b.setTargetUserId(target.getId());
      b.setStatus(UserConnection.Status.ACCEPTED);
      b.setCreatedBy(requesterId);
      b.setCreatedAt(Instant.now());
      b.setUpdatedAt(Instant.now());
      return connRepo.save(b);
    }

    // New pending
    var edge = new UserConnection();
    edge.setUserId(requesterId);
    edge.setTargetUserId(target.getId());
    edge.setStatus(UserConnection.Status.PENDING);
    edge.setCreatedBy(requesterId);
    edge.setCreatedAt(Instant.now());
    edge.setUpdatedAt(Instant.now());
    return connRepo.save(edge);
  }

  /** Accept a pending request where 'me' is the target. */
  @Transactional
  public UserConnection accept(Long me, Long connectionId) {
    UserConnection uc = connRepo.findById(connectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));

    if (!uc.getTargetUserId().equals(me)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the target user can accept");
    }

    uc.setStatus(UserConnection.Status.ACCEPTED);
    uc.setUpdatedAt(Instant.now());
    return connRepo.save(uc);
  }

  /** Delete/cancel/decline a connection the current user is part of. */
  @Transactional
  public void delete(long me, Long connectionId) {
    UserConnection uc = connRepo.findById(connectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));

    // allow delete if requester or target is 'me'
    if ((!uc.getUserId().equals(me)) && 
    		(!uc.getTargetUserId().equals(me))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this connection");
    }
    connRepo.delete(uc);
  }
}
