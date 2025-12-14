package com.porasl.authservices.service;

import java.time.Instant;
import java.util.ArrayList;
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

    // ==========================================================
    // LIST FRIENDS
    // ==========================================================

    public List<FriendSummaryDto> listAcceptedConnections(Long userId) {

        List<User> requesterSide =
                connRepo.findAcceptedTargets(userId, Status.ACCEPTED);

        List<User> targetSide =
                connRepo.findAcceptedRequesters(userId, Status.ACCEPTED);

        List<User> merged = new ArrayList<>();
        merged.addAll(requesterSide);
        merged.addAll(targetSide);

        return merged.stream()
                .map(FriendMapper::toDto)
                .toList();
    }

    // ==========================================================
    // REQUEST CONNECTION
    // ==========================================================

    @Transactional
    public ConnectionDto request(Long requesterId, Long targetId) {

        if (requesterId.equals(targetId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "cannot connect to self");
        }

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "requester not found"));

        User target = userRepo.findById(targetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "target not found"));

        // Already exists?
        var existingOpt =
                connRepo.findByRequesterIdAndTargetId(requesterId, targetId);

        if (existingOpt.isPresent()) {
            return ConnectionDto.of(existingOpt.get(), true);
        }

        // Reverse pending → accept it
        var reverseOpt =
                connRepo.findByRequesterIdAndTargetIdAndStatus(
                        targetId, requesterId, Status.PENDING);

        if (reverseOpt.isPresent()) {
            UserConnection reverse = reverseOpt.get();
            reverse.setStatus(Status.ACCEPTED);
            reverse.setUpdatedAt(Instant.now());
            connRepo.save(reverse);

            return ConnectionDto.of(reverse, true);
        }

        // Create new pending
        UserConnection edge = new UserConnection();
        edge.setRequester(requester);
        edge.setTarget(target);
        edge.setStatus(Status.PENDING);
        edge.setCreatedBy(requesterId);
        edge.setCreatedAt(Instant.now());
        edge.setUpdatedAt(Instant.now());

        UserConnection saved = connRepo.save(edge);
        return ConnectionDto.of(saved, true);
    }

    // ==========================================================
    // REQUEST BY EMAIL (PLACEHOLDER SUPPORT)
    // ==========================================================

    @Transactional
    public UserConnection createConnectionRrequestByEmail(
            long requesterUserId,
            String targetEmail,
            String notes) {

        if (targetEmail == null || targetEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        String email = targetEmail.trim().toLowerCase();

        User target = userRepo.findByEmailIgnoreCaseAndIsPlaceholderFalse(email)
                .orElseGet(() ->
                        userRepo.findByEmailIgnoreCase(email).orElse(null));

        if (target == null) {
            target = ((UserBuilder) User.builder()
                    .email(email)
                    .isPlaceholder(true)
                    .accountNonLocked(true))
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();

            target = userRepo.save(target);
        }

        if (requesterUserId == target.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "cannot connect to self");
        }

        // ✅ FIX: make target effectively final for lambda
        final User resolvedTarget = target;

        return connRepo
                .findByRequesterIdAndTargetId(requesterUserId, resolvedTarget.getId())
                .orElseGet(() -> {
                    UserConnection uc = new UserConnection();
                    uc.setRequester(userRepo.getReferenceById(requesterUserId));
                    uc.setTarget(resolvedTarget);   // ✅ now legal
                    uc.setStatus(Status.PENDING);
                    uc.setNote(notes);
                    uc.setCreatedBy(requesterUserId);
                    uc.setCreatedAt(Instant.now());
                    uc.setUpdatedAt(Instant.now());
                    return connRepo.save(uc);
                });
    }

    // ==========================================================
    // ACCEPT
    // ==========================================================

    @Transactional
    public UserConnection accept(Long me, Long connectionId) {

        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Connection not found"));

        if (uc.getTarget().getId() != me.longValue()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the target may accept");
        }

        uc.setStatus(Status.ACCEPTED);
        uc.setUpdatedAt(Instant.now());
        return connRepo.save(uc);
    }

    // ==========================================================
    // DELETE
    // ==========================================================

    @Transactional
    public void delete(long me, Long connectionId) {

        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Connection not found"));

        if (uc.getRequester().getId() != me
                && uc.getTarget().getId() != me) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not allowed to delete this connection");
        }

        connRepo.delete(uc);
    }
}
