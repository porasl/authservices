package com.porasl.authservices.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.porasl.authservices.auth.AuthenticationRequest;
import com.porasl.authservices.auth.AuthenticationResponse;
import com.porasl.authservices.auth.RegisterRequest;
import com.porasl.authservices.token.Token;
import com.porasl.authservices.token.TokenRepository;
import com.porasl.authservices.token.TokenType;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  @Autowired
  UserRepository userRepository;
  @Autowired
  TokenRepository tokenRepository;
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired
 JwtService jwtService;
  @Autowired
  AuthenticationManager authenticationManager; 

  public AuthenticationResponse register(RegisterRequest request) {
	  String email = request.getEmail().trim().toLowerCase();

	    // 1) See if there is a placeholder user for this email
	    User user = userRepository.findByEmailIgnoreCaseAndIsPlaceholderTrue(email)
	            .orElse(null);
	  
	    if (user != null) {
	        // ✅ "Upgrade" placeholder -> real user
	        user.setFirstname(request.getFirstname());
	        user.setLastname(request.getLastname());
	        user.setPassword(passwordEncoder.encode(request.getPassword()));
	        user.setIsPlaceholder(false);
	        user.setEnabled(true);  // or whatever your flow is
	    }else {
        user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname()) 
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .status(false)
        .approved(false)
        .blocked(false)
        .activationcode(Long.toHexString(Double.doubleToLongBits(Math.random())))
        .profileImageUrl(request.getProfileImageUrl())
        .createdDate(new Date().getTime())
        .updatedDate(new Date().getTime())
        .build();
	    }
    Optional<User> savedUser = userRepository.findByEmailIgnoreCase(user.getEmail());
    if (savedUser.isEmpty()) {
      savedUser = Optional.of(userRepository.save(user));
    }

    // ▶ issue richer access token (with claims) + refresh
    var accessToken  = jwtService.generateAccessToken(savedUser.get());
    var refreshToken = jwtService.generateRefreshToken(savedUser.get());

    saveUserToken(savedUser.get(), accessToken);

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .firstname(savedUser.get().getFirstname())
        .lastname(savedUser.get().getLastname())
        .profileImageUrl(savedUser.get().getProfileImageUrl())
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    var userOpt = userRepository.findByEmailIgnoreCase(request.getEmail());
    var retrievedUser = userOpt.orElseThrow(() -> new IllegalStateException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), retrievedUser.getPassword())) {
      throw new IllegalStateException("Wrong password");
    }

    // ▶ rotate tokens
    var accessToken  = jwtService.generateAccessToken(retrievedUser);
    var refreshToken = jwtService.generateRefreshToken(retrievedUser);
    revokeAllUserTokens(retrievedUser);
    saveUserToken(retrievedUser, accessToken);

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .firstname(retrievedUser.getFirstname())
        .lastname(retrievedUser.getLastname())
        .profileImageUrl(retrievedUser.getProfileImageUrl())
        .build();
  }

  /**
   * Authenticate by presenting a valid (non-expired) Bearer token + the expected email.
   * If the token subject matches the email, we rotate and return fresh access/refresh.
   */
  public AuthenticationResponse authenticateWithToken(
      HttpServletRequest request,
      AuthenticationRequest authenticationRequest) {

    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalStateException("Missing Bearer token");
    }
    final String token = authHeader.substring(7);
    final String emailFromBody = authenticationRequest.getEmail();

    var subject = jwtService.extractUsername(token);
    if (subject == null || emailFromBody == null ||
        !subject.trim().equalsIgnoreCase(emailFromBody.trim())) {
      throw new IllegalStateException("Wrong email address");
    }

    var user = userRepository.findByEmailIgnoreCase(subject)
        .orElseThrow(() -> new IllegalStateException("User not found"));

    // Optional: validate the presented token cryptographically & expiry
    if (!jwtService.isTokenValid(token, user)) {
      throw new IllegalStateException("Invalid or expired token");
    }

    // ▶ rotate and return fresh tokens
    var accessToken  = jwtService.generateAccessToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, accessToken);

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .firstname(user.getFirstname())
        .lastname(user.getLastname())
        .profileImageUrl(user.getProfileImageUrl())
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUserId(user.getId());
    if (validUserTokens.isEmpty()) return;
    validUserTokens.forEach(t -> {
      t.setExpired(true);
      t.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }
    final String refreshToken = authHeader.substring(7);
    final String userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {
      var user = userRepository.findByEmailIgnoreCase(userEmail).orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateAccessToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        var authResponse = AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .profileImageUrl(user.getProfileImageUrl())
            .build();

        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  public String getToken(HttpServletRequest request) {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return "";
    }
    return authHeader.substring(7);
  }
}
