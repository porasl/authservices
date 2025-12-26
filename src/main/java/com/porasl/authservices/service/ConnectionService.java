package com.porasl.authservices.service;

import java.time.Instant;
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
import com.porasl.common.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final UserRepository userRepo;
    private final UserConnectionRepository connRepo;

    // ==========================================================
    // LIST Connections (FIXED)
    // ==========================================================

    public List<FriendSummaryDto> listAcceptedConnections(Long userId) {

        User me = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        List<UserConnection> connections =
                connRepo.findAllByRequesterOrTargetAndStatus(
                        me, me, Status.ACCEPTED);

        return connections.stream()
                .map(c -> FriendMapper.toDto(c, me)) // ✅ FIXED
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

        var existing =
                connRepo.findByRequesterIdAndTargetId(requesterId, targetId);

        if (existing.isPresent()) {
            return ConnectionDto.of(existing.get(), true);
        }

        var reverse =
                connRepo.findByRequesterIdAndTargetIdAndStatus(
                        targetId, requesterId, Status.PENDING);

        if (reverse.isPresent()) {
            UserConnection uc = reverse.get();
            uc.setStatus(Status.ACCEPTED);
            uc.setUpdatedAt(Instant.now());
            return ConnectionDto.of(connRepo.save(uc), true);
        }

        UserConnection uc = new UserConnection();
        uc.setRequester(requester);
        uc.setTarget(target);
        uc.setStatus(Status.PENDING);
        uc.setCreatedBy(requesterId);
        uc.setCreatedAt(Instant.now());
        uc.setUpdatedAt(Instant.now());

        return ConnectionDto.of(connRepo.save(uc), true);
    }

    // ==========================================================
    // REQUEST BY EMAIL
    // ==========================================================

    @Transactional
    public UserConnection createConnectionRequestByEmail(
            long requesterUserId,
            String targetEmail,
            String notes) {

        if (targetEmail == null || targetEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        String email = targetEmail.trim().toLowerCase();

        User target = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        if (requesterUserId == target.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "cannot connect to self");
        }

        return connRepo
                .findByRequesterIdAndTargetId(requesterUserId, target.getId())
                .orElseGet(() -> {
                    UserConnection uc = new UserConnection();
                    uc.setRequester(userRepo.getReferenceById(requesterUserId));
                    uc.setTarget(target);
                    uc.setStatus(Status.PENDING);
                    uc.setNote(notes);
                    uc.setCreatedBy(requesterUserId);
                    uc.setCreatedAt(Instant.now());
                    uc.setUpdatedAt(Instant.now());
                    return connRepo.save(uc);
                });
    }

    // ==========================================================
    // ACCEPT Connection
    // ==========================================================

    @Transactional
    public UserConnection accept(Long me, Long connectionId) {

        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Connection not found"));

        if (uc.getTarget().getId() !=me) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the target may accept");
        }

        uc.setStatus(Status.ACCEPTED);
        uc.setUpdatedAt(Instant.now());
        return connRepo.save(uc);
    }

    // ==========================================================
    // DELETE Connection
    // ==========================================================

    @Transactional
    public void delete(long me, Long connectionId) {

        UserConnection uc = connRepo.findById(connectionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Connection not found"));

        if ((uc.getRequester().getId() != me)
                && (uc.getTarget().getId() != me)){

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not allowed to delete this connection");
        }

        connRepo.delete(uc);
    }
}
