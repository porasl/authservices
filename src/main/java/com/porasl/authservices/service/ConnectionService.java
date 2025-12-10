package com.porasl.authservices.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.FriendMapper;
import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.connection.UserConnection.Status;
import com.porasl.authservices.connection.UserConnectionRepository;
import com.porasl.authservices.dto.ConnectionDto;
import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.User.UserBuilder;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final UserRepository userRepo;
    private final UserConnectionRepository connRepo;      

    public List<FriendSummaryDto> listAcceptedConnections(Long userId) {

        List<User> requesterSide =
                connRepo.findAcceptedTargets(userId, UserConnection.Status.ACCEPTED);

        List<User> targetSide =
                connRepo.findAcceptedRequesters(userId, UserConnection.Status.ACCEPTED);

        List<User> merged = new ArrayList<>();
        merged.addAll(requesterSide);
        merged.addAll(targetSide);

        return merged.stream()
                .map(FriendMapper::toDto)
                .toList();
    }


    /** Create a connection request or accept if reverse pending exists. */
    @Transactional
    public ConnectionDto request(Long requesterId, Long targetId) {

        if (requesterId.equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
        }

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "requester not found"));

        User target = userRepo.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "target not found"));

        // Already exists?
        var existing = connRepo.findByRequesterIdAndTargetId(requesterId, targetId);
        if (existing.isPresent()) {
            return ConnectionDto.of(existing.get(), false);
        }

        // Reverse pending
        var reverse = connRepo.findByRequesterIdAndTargetIdAndStatus(
                targetId, requesterId, UserConnection.Status.PENDING);

        if (reverse != null) {
            reverse.setStatus(UserConnection.Status.ACCEPTED);
            reverse.setUpdatedAt(Instant.now());
            connRepo.save(reverse);

            var b = new UserConnection();
            b.setRequester(requester);
            b.setTarget(target);
            b.setStatus(UserConnection.Status.ACCEPTED);
            b.setCreatedBy(requesterId);
            b.setCreatedAt(Instant.now());
            b.setUpdatedAt(Instant.now());

            var saved = connRepo.save(b);
            return ConnectionDto.of(saved, true);
        }

        // Create new pending
        var edge = new UserConnection();
        edge.setRequester(requester);
        edge.setTarget(target);
        edge.setStatus(UserConnection.Status.PENDING);
        edge.setCreatedBy(requesterId);
        edge.setCreatedAt(Instant.now());
        edge.setUpdatedAt(Instant.now());

        var saved = connRepo.save(edge);
        return ConnectionDto.of(saved, true);
    }

    @Transactional
    public UserConnection createConnectionRrequestByEmail(long requester_user_id,long target_user_id, String targetEmail) {
        if (targetEmail == null || targetEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        String email = targetEmail.trim().toLowerCase();

        // 1) If a real user already exists -> use them
        User target = userRepo.findByEmailIgnoreCaseAndIsPlaceholderFalse(email)
                .orElseGet(() -> userRepo.findByEmailIgnoreCase(email).orElse(null));

        // 2) If nobody exists at all -> create placeholder user
        if (target == null) {
            target = ((UserBuilder) User.builder()
                    .email(email)
                    .firstname(null)          // or "Pending"
                    .lastname(null)
                    .password(null)          // no password yet
                    .isPlaceholder(true)     // <--- important
                    .accountNonLocked(true))
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();

            target = userRepo.save(target);
        }

        if (requester_user_id == target.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot connect to self");
        }

        // 3) Check for existing connection
        Object existing = connRepo.findByUserIdAndTargetUserId(target_user_id, target.getId());
        

        // 4) Reverse pending? -> accept both
        UserConnection reversePending = connRepo.findByRequesterIdAndTargetIdAndStatus(
                target.getId(), target_user_id, UserConnection.Status.PENDING);
        if (reversePending!=null) {
        	reversePending.setStatus(Status.ACCEPTED);
        	reversePending.setUpdatedAt(Instant.now());
        	connRepo.save(reversePending);

            var b = new UserConnection();
            b.setId(userId);
            b.set(target.getId());
            b.setStatus(UserConnection.Status.ACCEPTED);
            b.setCreatedBy(userId);
            b.setCreatedAt(Instant.now());
            b.setUpdatedAt(Instant.now());
            return connRepo.save(b);
        }

        // 5) Otherwise create new PENDING edge
        var edge = new UserConnection();
        edge.setId(userId);
        edge.setTa(target.getId());
        edge.setStatus(UserConnection.Status.PENDING);
        edge.setCreatedBy(userId);
        edge.setCreatedAt(Instant.now());
        edge.setUpdatedAt(Instant.now());
        return connRepo.save(edge);
    }


    /** Accept a pending request. */
    @Transactional
    public UserConnection accept(Long me, Long connectionId) {
        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));

        if (uc.getTarget().getId() != me) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the target may accept");
        }

        uc.setStatus(UserConnection.Status.ACCEPTED);
        uc.setUpdatedAt(Instant.now());
        return connRepo.save(uc);
    }

    /** Delete/cancel/decline. */
    @Transactional
    public void delete(long me, Long connectionId) {
        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));

        if (uc.getRequester().getId() != me &&
            uc.getTarget().getId() != me) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this connection");
        }

        connRepo.delete(uc);
    }
}
