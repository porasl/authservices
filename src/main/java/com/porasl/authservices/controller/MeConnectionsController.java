package com.porasl.authservices.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.common.dto.FriendSummaryDto;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserService;

import com.porasl.common.utils.Utils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/me")
@RequiredArgsConstructor
public class MeConnectionsController {

    private static final Logger log = LoggerFactory.getLogger(MeConnectionsController.class);

    private final ConnectionService connectionService;
    private final UserService userService;

    // ---- Create a connection request ----
    @PostMapping(path = "/connections", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FriendSummaryDto> createByEmail(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateConnectionByEmailReq req) {

        log.debug("POST /connections called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);

        if (req == null || req.getTargetEmail() == null || req.getTargetEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        log.info("User {} creating connection request for {}", meId, req.getTargetEmail());
        UserConnection created = connectionService.createConnectionRequestByEmail(meId, req.getTargetEmail().trim(),req.getNotes());
        FriendSummaryDto friendSummaryDto = new FriendSummaryDto( 
        		created.getId(),
        		created.getTarget().getEmail(),
        	    created.getTarget().getFirstname(),
        	    created.getTarget().getLastname(),
        	    created.getTarget().getProfileImageUrl(),
        	    created.getTarget().getCreatedDate(),
        	    created.getNote(),
        	    created.getRequester().getId(),
        	    created.getTarget().getId());
        				
        				
        return ResponseEntity.status(HttpStatus.CREATED).body(friendSummaryDto);
    }

    @GetMapping(value = "/connections/accepted", produces = "application/json")
    public ResponseEntity<List<FriendSummaryDto>> listAccepted(@AuthenticationPrincipal Object principal) {
        log.debug("GET /connections/accepted called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);
        log.info("Listing accepted connections for user {}", meId);

        List<FriendSummaryDto> out = connectionService.listAcceptedConnections(meId);
        return ResponseEntity.ok(out == null ? java.util.Collections.emptyList() : out);
    }

    // Alias
    @GetMapping(value = "/connections", produces = "application/json")
    public ResponseEntity<List<FriendSummaryDto>> listFriendsAlias(@AuthenticationPrincipal Object principal) {
        log.debug("GET /me/connections called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);
        log.info("Listing friends (alias) for user {}", meId);

        List<FriendSummaryDto> out = connectionService.listAcceptedConnections(meId);

        return ResponseEntity.ok(out == null ? java.util.Collections.emptyList() : out);
    }

    @PostMapping(value = "/connections/{id}/accept", produces = "application/json")
    public ResponseEntity<UserConnection> accept(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") Long connectionId) {

        log.debug("POST /connections/{}/accept called with principal: {}", connectionId, Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);
        log.info("User {} accepting connection {}", meId, connectionId);

        return ResponseEntity.ok(connectionService.accept(meId, connectionId));
    }

    @DeleteMapping("/connections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") Long connectionId) {

        log.debug("DELETE /connections/{} called with principal: {}", connectionId, Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);
        log.info("User {} deleting connection {}", meId, connectionId);

        connectionService.delete(meId, connectionId);
    }

    // --------------------------
    // Helper Methods
    // --------------------------

    private Long requireCurrentUserId(Object principal) {
        Optional<Long> id = tryResolveUserId(principal);
        if (id.isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.warn("No @AuthenticationPrincipal resolved. Context auth={} principal={}",
                    (auth == null ? null : auth.getClass().getName()),
                    (auth == null ? null : Utils.principalInfo(auth.getPrincipal())));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        }
        return id.get();
    }

    private Optional<Long> tryResolveUserId(Object principal) {
        if (principal == null) return Optional.empty();

        if (principal instanceof User u) {
            log.debug("Resolved principal as domain User: {}", u.getEmail());
            return Optional.ofNullable(u.getId());
        }
        if (principal instanceof UserDetails ud) {
            log.debug("Resolved principal as UserDetails: {}", ud.getUsername());
            return userService.findByEmail(ud.getUsername())
                    .map(User::getId)
                    .or(() -> userService.findByUsername(ud.getUsername()).map(User::getId));
        }
        if (principal instanceof String s && !s.isBlank()) {
            log.debug("Resolved principal as String: {}", s);
            return userService.findByEmail(s)
                    .map(User::getId)
                    .or(() -> userService.findByUsername(s).map(User::getId));
        }
        if (principal instanceof Principal p) {
            log.debug("Resolved principal as java.security.Principal: {}", p.getName());
            return userService.findByEmail(p.getName())
                    .map(User::getId)
                    .or(() -> userService.findByUsername(p.getName()).map(User::getId));
        }
        return Optional.empty();
    }

  
}
