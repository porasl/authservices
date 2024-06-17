package com.inrik.authservices.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inrik.authservices.config.JwtService;
import com.inrik.authservices.user.ChangePasswordRequest;
import com.inrik.authservices.user.DeleteUserRequest;
import com.inrik.authservices.user.UserService;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authservice;
  private final UserService userService;
  private final JwtService jwtService;

  
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(authservice.register(request));
  }
  
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authservice.authenticate(request));
  }
  
  @PostMapping("/activate")
  public ResponseEntity<Boolean> activate(
      @RequestBody ActivateRequest request
  ) {
    return ResponseEntity.ok(userService.activate(request));
  }
  

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
	  authservice.refreshToken(request, response);
  }

  @PatchMapping("/changePasswordByUser")
  public ResponseEntity<?> changePasswordByUser(
        @RequestBody ChangePasswordRequest changeRequest, HttpServletRequest request) {
	  String token = request.getHeader("Authorization");
      token = token.substring(7, token.length());
	  
	  if(!jwtService.isTokenValid(token)) {
		  System.err.println("Token is expired");
		  return ResponseEntity.badRequest().build();
	  }
	  String userId = jwtService.extractUsername(token); 
	  userService.changePasswordByUser(userId, changeRequest);
      return ResponseEntity.ok().build();
  }
  
  @PatchMapping("/changePasswordByAdmin")
  public ResponseEntity<?> changePasswordByAdmin(
        @RequestBody ChangePasswordRequest changeRequest, HttpServletRequest request) {
	  String token = request.getHeader("Authorization");
      token = token.substring(7, token.length());
	  
	  if(!jwtService.isTokenValid(token)) {
		  System.err.println("Token is expired");
		  return ResponseEntity.badRequest().build();
	  }
	  String adminUserId = jwtService.extractUsername(token); 
	 
	  userService.changePasswordByAdmin(changeRequest, adminUserId);
      return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/deleteUser")
  public ResponseEntity<?> deleteUser(
        @RequestBody DeleteUserRequest request, Principal connectedUser) {
	  userService.deleteUser(request, connectedUser);
      return ResponseEntity.ok().build();
  }


}