package com.porasl.authservices.service; // <- move out of .controller

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.connection.UserConnectionRepository;
import com.porasl.authservices.dto.ConnectionDto;
import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final UserRepository userRepo;
  private final UserConnectionRepository connRepo;
  private final UserConnectionRepository repo;
  
  public List<FriendSummaryDto> listAcceptedConnections(Long userId) {
	    List<com.porasl.authservices.user.User> friends =
	        repo.findAcceptedCounterparties(userId, ConnectionStatus.ACCEPTED);

	    // never return null — always an array (possibly empty)
	    if (friends == null || friends.isEmpty()) return java.util.Collections.emptyList();

	    return friends.stream()
	        .map(FriendMapper::toDto)
	        .toList();
	  }
  
  @Transactional
  public ConnectionDto request(Long requesterId, Long targetId) {
    if (Objects.equals(requesterId, targetId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
    }

    userRepo.findById(targetId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "target not found"));

    // already exists?
    var existing = connRepo.findByUserIdAndTargetUserId(requesterId, targetId);
    if (existing.isPresent()) return ConnectionDto.of(existing.get(), /*created=*/false);

    // reverse pending? → accept both directions
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

    // create new pending edge
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

  public List<FriendSummaryDto> listAcceptedConnections(Long userId) {
	    List<FriendSummaryDto> out = repoOrMapper(userId);
	    return (out != null) ? out : java.util.Collections.emptyList();
	}

public UserConnection requestByEmail(long id, String targetEmail) {
	// TODO Auto-generated method stub
	return null;
}

@Transactional
public UserConnection accept(Long me, Long connectionId) {   // 👈 return type MUST be UserConnection
  UserConnection uc = connRepo.findById(connectionId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));

  if (!Objects.equals(uc.getTargetUserId(), me)) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the target user can accept");
  }

  uc.setStatus(UserConnection.Status.ACCEPTED);
  uc.setUpdatedAt(Instant.now());  // or setUpdatedAt(Instant.now()) if you don't have helper
  return connRepo.save(uc);                                  // 👈 returns UserConnection
}

public void delete(long id, Long connectionId) {
	// TODO Auto-generated method stub
	
}
}
