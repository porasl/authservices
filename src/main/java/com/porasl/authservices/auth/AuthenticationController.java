package com.porasl.authservices.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import springfox.documentation.annotations.ApiIgnore;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.io.IOException;
import java.security.Principal;

@Api(tags = "Authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authservice;
  private final UserService userService;
  private final JwtService jwtService;

  @ApiOperation(value = "Register a new user")
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
		@ApiParam(value = "Register request body") @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(authservice.register(request));
  }
  
  @ApiOperation(value = "Authenticate a user")
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate( 
	 @ApiParam(value = "Authentication request body") @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authservice.authenticate(request));
  }
  
  @ApiOperation(value = "Authenticate a user with token")
  @PostMapping("/authenticateWithToken")
  public ResponseEntity<AuthenticationResponse> authenticateWithToken(HttpServletRequest request,
		  @RequestBody AuthenticationRequest authenticationRequest) throws IOException{
    return ResponseEntity.ok(authservice.authenticateWithToken(request, authenticationRequest));
  }
  
  @ApiOperation(value = "Activate a user")
  @PostMapping("/activate")
  public ResponseEntity<Boolean> activate(
		  @ApiParam(value = "Activate request body") @RequestBody ActivateRequest request
  ) {
    return ResponseEntity.ok(userService.activate(request));
  }
  
  @ApiOperation(value = "Refresh token")
  @PostMapping("/refresh-token")
  public void refreshToken(
		  @ApiIgnore HttpServletRequest request,
		  @ApiIgnore HttpServletResponse response
  ) throws IOException {
	  authservice.refreshToken(request, response);
  }

  @ApiOperation(value = "Change password by user")
  @PatchMapping("/changePasswordByUser")
  public ResponseEntity<?> changePasswordByUser(
		  @ApiParam(value = "Change password request body") @RequestBody ChangePasswordRequest changeRequest) {
	 
	  userService.changePasswordByUser(changeRequest);
      return ResponseEntity.ok().build();
  }
  
  @ApiOperation(value = "Change password by admin")
  @PatchMapping("/changePasswordByAdmin")
  public ResponseEntity<?> changePasswordByAdmin(
		  @ApiParam(value = "Change password request body") @RequestBody ChangePasswordRequest changeRequest,
		  @ApiIgnore HttpServletRequest request) {
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
  
  @ApiOperation(value = "Delete user")
  @DeleteMapping("/deleteUser")
  public ResponseEntity<?> deleteUser(
		  @ApiParam(value = "Delete user request body") 
		  @RequestBody DeleteUserRequest request, 
		  @ApiIgnore Principal connectedUser) {
	  userService.deleteUser(request, connectedUser);
      return ResponseEntity.ok().build();
  }


}