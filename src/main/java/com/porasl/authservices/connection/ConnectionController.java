package com.porasl.authservices.connection;


import com.porasl.authservices.user.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/connections")
public class ConnectionController {

private final ConnectionService service;

public ConnectionController(ConnectionService service) {
 this.service = service;
}

@GetMapping
public List<Map<String, Object>> list(@PathVariable long userId) {
 return service.listAcceptedConnections(userId).stream().map(this::toDto).toList();
}

@PostMapping
public Map<String, Object> add(@PathVariable long userId,
                              @RequestBody Map<String, String> body) {
 long targetUserId = Long.parseLong(body.get("targetUserId"));
 User created = service.addAcceptedConnection(userId, targetUserId);
 return toDto(created);
}

private Map<String, Object> toDto(User u) {
 return Map.of(
   "id", u.getId(),
   "firstname", u.getFirstname(),
   "lastname", u.getLastname(),
   "email", u.getEmail(),
   "profileImageUrl", u.getProfileImageUrl()
   // "phoneNumber", ???  <-- your current User class has no phone field.
   // If you need it, add `private String phoneNumber;` to User and re-run with ddl-auto=update.
 );
}
}
