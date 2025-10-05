package com.porasl.authservices.controller;

//package com.porasl.authservices.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.service.ConnectionService;
import com.porasl.authservices.user.User;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth/api/me/connections")
@RequiredArgsConstructor
public class MeConnectionsController {

private final ConnectionService connectionService;

@GetMapping("/accepted")
public List<FriendSummaryDto> listAccepted(@AuthenticationPrincipal User me) {
 if (me == null) {
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
 }
 return connectionService.listAcceptedConnections(me.getId());
}

// ... your POST (createByEmail) stays as-is ...
}


@GetMapping("/friends")
public List<FriendSummaryDto> listFriends(@AuthenticationPrincipal User requester) {
    return friendService.listFriends(requester);
}
