package com.porasl.authservices.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import com.porasl.authservices.connection.ConnectionService;
import com.porasl.authservices.dto.CreateConnectionByEmailReq;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/api/me/connections")
@RequiredArgsConstructor
public class MeConnectionsController {
  private final ConnectionService connectionService;
  private final UserRepository userRepo; // has findByEmailIgnoreCase

  @PostMapping
  public ResponseEntity<ConnectionDto> createByEmail(
      @AuthenticationPrincipal User requester,
      @Valid @RequestBody CreateConnectionByEmailReq req) {

    String email = req.getTargetEmail().trim().toLowerCase();
    User target = userRepo.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new NotFound("target not found"));

    var dto = connectionService.request(requester.getId(), target.getId());
    return new ResponseEntity<>(dto, dto.isNew() ? HttpStatus.CREATED : HttpStatus.OK);
  }
}
