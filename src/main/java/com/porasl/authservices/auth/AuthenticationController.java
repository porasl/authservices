package com.porasl.authservices.auth;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.config.JwtService;
import com.porasl.authservices.user.ChangePasswordRequest;
import com.porasl.authservices.user.DeleteUserRequest;
import com.porasl.authservices.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication", description = "Endpoints for user authentication and account management")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authservice;
  private final UserService userService;
  private final JwtService jwtService;

  @Operation(summary = "Register a new user")
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @Parameter(description = "Register request body")
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(authservice.register(request));
  }

  @Operation(summary = "Authenticate a user")
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Parameter(description = "Authentication request body") @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authservice.authenticate(request));
  }

  @Operation(summary = "Authenticate a user with token")
  @PostMapping("/authenticateWithToken")
  public ResponseEntity<AuthenticationResponse> authenticateWithToken(
      HttpServletRequest request,
      @RequestBody AuthenticationRequest authenticationRequest
  ) throws IOException {
    return ResponseEntity.ok(authservice.authenticateWithToken(request, authenticationRequest));
  }

  @Operation(summary = "Activate a user")
  @PostMapping("/activate")
  public ResponseEntity<Boolean> activate(
      @Parameter(description = "Activate request body") @RequestBody ActivateRequest request
  ) {
    return ResponseEntity.ok(userService.activate(request));
  }

  @Operation(summary = "Refresh token")
  @PostMapping("/refresh-token")
  public void refreshToken(
       HttpServletRequest request,
       HttpServletResponse response
  ) throws IOException {
    authservice.refreshToken(request, response);
  }

  @Operation(summary = "Change password by user")
  @PatchMapping("/changePasswordByUser")
  public ResponseEntity<?> changePasswordByUser(
      @Parameter(description = "Change password request body") @RequestBody ChangePasswordRequest changeRequest
  ) {
    userService.changePasswordByUser(changeRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Change password by admin")
  @PatchMapping("/changePasswordByAdmin")
  public ResponseEntity<?> changePasswordByAdmin(
      @Parameter(description = "Change password request body") @RequestBody ChangePasswordRequest changeRequest,
       HttpServletRequest request
  ) {
    String token = request.getHeader("Authorization");
    token = token.substring(7); // remove "Bearer "

    if (!jwtService.isTokenValid(token)) {
      System.err.println("Token is expired");
      return ResponseEntity.badRequest().build();
    }

    String adminUserId = jwtService.extractUsername(token);
    userService.changePasswordByAdmin(changeRequest, adminUserId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete user")
  @DeleteMapping("/deleteUser")
  public ResponseEntity<?> deleteUser(
      @Parameter(description = "Delete user request body") @RequestBody DeleteUserRequest request,
       Principal connectedUser
  ) {
    userService.deleteUser(request, connectedUser);
    return ResponseEntity.ok().build();
  }
}
