package com.porasl.authservices.controller;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.auth.ActivateRequest;
import com.porasl.authservices.auth.AuthenticationRequest;
import com.porasl.authservices.auth.AuthenticationResponse;
import com.porasl.authservices.auth.RegisterRequest;
import com.porasl.authservices.service.AuthenticationService;
import com.porasl.authservices.service.JwtService;
import com.porasl.authservices.user.ChangePasswordRequest;
import com.porasl.authservices.user.DeleteUserRequest;
import com.porasl.authservices.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Authentication", description = "Endpoints for user authentication and account management")
@RestController
@RequestMapping("/auth") // <-- CHANGED from /internal/auth to /auth
public class AuthenticationController {

  private final AuthenticationService authservice;
  private final UserService userService;
  private final JwtService jwtService;

  public AuthenticationController(
      AuthenticationService authservice,
      UserService userService,
      JwtService jwtService
  ) {
    this.authservice = authservice;
    this.userService = userService;
    this.jwtService = jwtService;
  }

  // PUBLIC: create account
  @Operation(summary = "Register a new user")
  @PostMapping(value = "/register", consumes = "application/json")
  public ResponseEntity<AuthenticationResponse> register(
      @Parameter(description = "Register request body")
      @RequestBody RegisterRequest request
  ) {
    try {
      return ResponseEntity.ok(authservice.register(request));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  // PUBLIC: login with email/password
  @Operation(summary = "Authenticate a user")
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Parameter(description = "Authentication request body")
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authservice.authenticate(request));
  }

  // PUBLIC: login-with-token flow
  @Operation(summary = "Authenticate a user with token")
  @PostMapping("/authenticateWithToken")
  public ResponseEntity<AuthenticationResponse> authenticateWithToken(
      HttpServletRequest request,
      @RequestBody AuthenticationRequest authenticationRequest
  ) throws IOException {
    return ResponseEntity.ok(authservice.authenticateWithToken(request, authenticationRequest));
  }

  // PUBLIC (usually email link / activation code)
  @Operation(summary = "Activate a user")
  @PostMapping("/activate")
  public ResponseEntity<Boolean> activate(
      @Parameter(description = "Activate request body")
      @RequestBody ActivateRequest request
  ) {
    return ResponseEntity.ok(userService.activate(request));
  }

  // PUBLIC: exchange refresh token for new access token
  @Operation(summary = "Refresh token")
  @PostMapping("/refresh-token")
  public void refreshToken(
       HttpServletRequest request,
       HttpServletResponse response
  ) throws IOException {
    authservice.refreshToken(request, response);
  }

  // PROTECTED: user changing their own password (must be logged in with valid JWT)
  @Operation(summary = "Change password by user")
  @PatchMapping("/changePasswordByUser")
  public ResponseEntity<?> changePasswordByUser(
      @Parameter(description = "Change password request body")
      @RequestBody ChangePasswordRequest changeRequest
  ) {
    userService.changePasswordByUser(changeRequest);
    return ResponseEntity.ok().build();
  }

  // PROTECTED: admin changing someone else's password (JWT must be valid and must belong to admin)
  @Operation(summary = "Change password by admin")
  @PatchMapping("/changePasswordByAdmin")
  public ResponseEntity<?> changePasswordByAdmin(
      @Parameter(description = "Change password request body")
      @RequestBody ChangePasswordRequest changeRequest,
      HttpServletRequest request
  ) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String token = header.substring(7);

    if (!jwtService.isTokenValid(token)) {
      System.err.println("Token is expired or invalid");
      return ResponseEntity.badRequest().build();
    }

    String adminUserId = jwtService.extractUsername(token); // typically email
    userService.changePasswordByAdmin(changeRequest, adminUserId);
    return ResponseEntity.ok().build();
  }

  // PROTECTED: delete user account
  @Operation(summary = "Delete user")
  @DeleteMapping("/deleteUser")
  public ResponseEntity<?> deleteUser(
      @Parameter(description = "Delete user request body")
      @RequestBody DeleteUserRequest request,
      Principal connectedUser
  ) {
    userService.deleteUser(request, connectedUser);
    return ResponseEntity.ok().build();
  }
}
