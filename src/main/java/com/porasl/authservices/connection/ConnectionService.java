package com.porasl.authservices.connection;

//package com.porasl.authservices.connection;

import java.time.Instant;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.dto.ConnectionDto;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

private final UserRepository userRepo;
private final UserConnectionRepository connRepo;

// keep a 2-arg version if other code still calls it
@Transactional
public ConnectionDto request(Long requesterId, Long targetId) {
 return request(requesterId, targetId, null);
}

@Transactional
public ConnectionDto request(Long requesterId, Long targetId, String note) {
 if (Objects.equals(requesterId, targetId)) {
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
 }

 userRepo.findById(targetId)
     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "target not found"));

 
 // already exists?
 var existing = connRepo.findByUserIdAndTargetUserId(requesterId, targetId);
 if (existing.isPresent()) {
   // Optionally update note on existing pending edge:
   var uc = existing.get();
   if (uc.getStatus() == UserConnection.Status.PENDING && note != null && !note.isBlank()) {
     uc.setNote(trim255(note));
     uc.setUpdatedAt(Instant.now());
     connRepo.save(uc);
   }
   return ConnectionDto.of(uc, /*created=*/false);
 }

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
   b.setNote(trim255(note));
   b.setCreatedBy(requesterId);
   b.setCreatedAt(Instant.now());
   b.setUpdatedAt(Instant.now());
   var saved = connRepo.save(b);

   return ConnectionDto.of(saved, /*created=*/true);
 }

 // otherwise: create new pending edge
 var edge = new UserConnection();
 edge.setUserId(requesterId);
 edge.setTargetUserId(targetId);
 edge.setStatus(UserConnection.Status.PENDING);
 edge.setNote(trim255(note));
 edge.setCreatedBy(requesterId);
 edge.setCreatedAt(Instant.now());
 edge.setUpdatedAt(Instant.now());
 var saved = connRepo.save(edge);

 return ConnectionDto.of(saved, /*created=*/true);
}

private String trim255(String s) {
 if (s == null) return null;
 var t = s.trim();
 return t.length() <= 255 ? t : t.substring(0, 255);
}
}
