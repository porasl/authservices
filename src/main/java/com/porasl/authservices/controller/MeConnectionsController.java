package com.porasl.authservices.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/api/me")
@RequiredArgsConstructor
public class MeConnectionsController {

    private final ConnectionService connectionService;

    // ---- Create a connection request (by target email) ----
    // POST /auth/api/me/connections
    @PostMapping("/connections")
    public ResponseEntity<UserConnection> createByEmail(
            @AuthenticationPrincipal User me,
            @Valid @RequestBody CreateConnectionByEmailReq req) {

        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        if (req == null || req.getTargetEmail() == null || req.getTargetEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetEmail is required");
        }

        UserConnection created = connectionService.requestByEmail(me.getId(), req.getTargetEmail().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/connections/accepted", produces = "application/json")
    public ResponseEntity<List<FriendSummaryDto>> listAccepted(@AuthenticationPrincipal User me) {
      if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
      List<FriendSummaryDto> out = connectionService.listAcceptedConnections(me.getId());
      return ResponseEntity.ok(out == null ? java.util.Collections.emptyList() : out);
    }
    
 // Alias still OK:
    @GetMapping(value = "/friends", produces = "application/json")
    public ResponseEntity<List<FriendSummaryDto>> listFriendsAlias(@AuthenticationPrincipal User me) {
      if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
      return ResponseEntity.ok(connectionService.listAcceptedConnections(me.getId()));
    }


    // ---- Accept a pending request (where I am the target) ----
    // POST /auth/api/me/connections/{id}/accept
    @PostMapping("/connections/{id}/accept")
    public ResponseEntity<UserConnection> accept(
            @AuthenticationPrincipal User me,
            @PathVariable("id") Long connectionId) {

        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        return ResponseEntity.ok(connectionService.accept(me.getId(), connectionId));
    }

    // ---- Decline/Cancel/Remove a connection ----
    // DELETE /auth/api/me/connections/{id}
    @DeleteMapping("/connections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal User me,
            @PathVariable("id") Long connectionId) {

        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        connectionService.delete(me.getId(), connectionId);
    }
}
