package com.porasl.authservices.controller;

import java.security.Principal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.authservices.dto.ConnectionRequestDto;
import com.porasl.common.dto.FriendSummaryDto;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserService;
import com.porasl.common.utils.Utils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class ApiConnectionsController {

    private static final Logger log = LoggerFactory.getLogger(ApiConnectionsController.class);

    private final ConnectionService connectionService;
    private final UserService userService;

    public ApiConnectionsController(ConnectionService connectionService, UserService userService) {
        this.connectionService = connectionService;
        this.userService = userService;
    }

    // Endpoint 1: POST /api/auth/me/connections
    @PostMapping(path = "/me/connections", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FriendSummaryDto> createMeConnection(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateConnectionByEmailReq req) {

        log.debug("POST /api/auth/me/connections called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);

        if (req == null || req.getTargetEmail() == null || req.getTargetEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        log.info("User {} creating connection request for {} via API", meId, req.getTargetEmail());
        UserConnection created = connectionService.createConnectionRequestByEmail(meId, req.getTargetEmail().trim(), req.getNotes());
        
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

    // Endpoint 2: POST /api/auth/connections/add
    @PostMapping(path = "/connections/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FriendSummaryDto> addConnection(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateConnectionByEmailReq req) {

        log.debug("POST /api/auth/connections/add called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);

        if (req == null || req.getTargetEmail() == null || req.getTargetEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        log.info("User {} adding connection for {} via API", meId, req.getTargetEmail());
        UserConnection created = connectionService.createConnectionRequestByEmail(meId, req.getTargetEmail().trim(), req.getNotes());
        
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

    // Endpoint 3: POST /api/auth/connections/request (with autoAccept support)
    @PostMapping(path = "/connections/request", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FriendSummaryDto> requestConnection(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody ConnectionRequestDto req) {

        log.debug("POST /api/auth/connections/request called with principal: {}", Utils.principalInfo(principal));
        Long meId = requireCurrentUserId(principal);

        if (req == null || req.getTargetEmail() == null || req.getTargetEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        log.info("User {} requesting connection for {} with autoAccept={} via API", meId, req.getTargetEmail(), req.isAutoAccept());
        
        UserConnection created;
        if (req.isAutoAccept()) {
            created = connectionService.createAndAutoAcceptConnectionByEmail(meId, req.getTargetEmail().trim(), req.getNotes());
        } else {
            created = connectionService.createConnectionRequestByEmail(meId, req.getTargetEmail().trim(), req.getNotes());
        }
        
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

    // --------------------------
    // Helper Methods (copied from MeConnectionsController)
    // --------------------------

    private Long requireCurrentUserId(Object principal) {
        Optional<Long> id = tryResolveUserId(principal);
        if (id.isEmpty()) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
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