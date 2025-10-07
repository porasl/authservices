package com.porasl.authservices.connection;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.authservices.user.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/api/me/connections")
@RequiredArgsConstructor
public class ConnectionController {

  private final ConnectionService connectionService;

  /** POST /auth/api/me/connections  — create a pending connection by target email */
  @PostMapping
  public ResponseEntity<UserConnection> createByEmail(
      @AuthenticationPrincipal User requester,
      @RequestBody CreateConnectionByEmailReq req) {

    if (requester == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UserConnection created = connectionService.requestByEmail(requester.getId(), req.getTargetEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** GET /auth/api/me/connections  — list accepted connections (friend summaries) */
  @GetMapping
  public ResponseEntity<List<FriendSummaryDto>> listAccepted(
      @AuthenticationPrincipal User requester) {

    if (requester == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    List<FriendSummaryDto> out = connectionService.listAcceptedConnections(requester.getId());
    return ResponseEntity.ok(out);
  }

  /** POST /auth/api/me/connections/{id}/accept — accept a pending request where I am target */
  @PostMapping("/{id}/accept")
  public ResponseEntity<Object> accept(
      @AuthenticationPrincipal User me,
      @PathVariable("id") Long connectionId) {

    if (me == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(connectionService.accept(me.getId(), connectionId));
  }

  /** DELETE /auth/api/me/connections/{id} — decline/cancel a pending connection (or remove) */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal User me,
      @PathVariable("id") Long connectionId) {

    if (me == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    connectionService.delete(me.getId(), connectionId);
    return ResponseEntity.noContent().build();
  }
}
