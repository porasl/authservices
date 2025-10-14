package com.porasl.authservices.controller; // ← adjust to your package

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.connection.UserConnection;
import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/api/me/connections")
@RequiredArgsConstructor
public class MeConnectionsController {

  private final ConnectionService connectionService;

  /** POST /auth/api/me/connections — create (or return existing) connection request by target email */
  @PostMapping
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

  /** GET /auth/api/me/connections/accepted — list my accepted connections */
  @GetMapping("/accepted")
  public List<FriendSummaryDto> listAccepted(@AuthenticationPrincipal User me) {
    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
    return connectionService.listAcceptedConnections(me.getId());
  }

  /** POST /auth/api/me/connections/{id}/accept — accept a pending request where I am the target */
  @PostMapping("/{id}/accept")
  public ResponseEntity<UserConnection> accept(
      @AuthenticationPrincipal User me,
      @PathVariable("id") Long connectionId) {

    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
    return ResponseEntity.ok(connectionService.accept(me.getId(), connectionId));
  }

  /** DELETE /auth/api/me/connections/{id} — decline/cancel/remove */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal User me,
      @PathVariable("id") Long connectionId) {

    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
    connectionService.delete(me.getId(), connectionId);
  }
}
