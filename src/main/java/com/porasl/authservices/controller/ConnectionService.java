package com.porasl.authservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.connection.UserConnectionRepository;
import com.porasl.authservices.dto.ConnectionDto;
import com.porasl.authservices.user.UserRepository;



@Service
public class ConnectionService {
  @Autowired UserRepository userRepo;             // your existing User JPA
  @Autowired UserConnectionRepository connRepo;

  @Transactional
  public ConnectionDto request(Long requesterId, Long targetId) {
    if (Objects.equals(requesterId, targetId)) throw new BadRequest("cannot connect to self");
    userRepo.findById(targetId).orElseThrow(() -> new NotFound("target not found"));

    // already exists?
    var existing = connRepo.findByUserIdAndTargetUserId(requesterId, targetId);
    if (existing.isPresent()) return ConnectionDto.of(existing.get(), /*isNew=*/false);

    // reverse pending? → accept both directions
    var reversePending = connRepo.findByUserIdAndTargetUserIdAndStatus(targetId, requesterId, UserConnection.Status.PENDING);
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
      connRepo.save(b);

      return ConnectionDto.of(b, /*isNew=*/true);
    }

    // create new pending edge
    var edge = new UserConnection();
    edge.setUserId(requesterId);
    edge.setTargetUserId(targetId);
    edge.setStatus(UserConnection.Status.PENDING);
    edge.setCreatedBy(requesterId);
    connRepo.save(edge);

    return ConnectionDto.of(edge, /*isNew=*/true);
  }
}
