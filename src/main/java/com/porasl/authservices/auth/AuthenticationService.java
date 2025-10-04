package com.porasl.authservices.auth;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.porasl.authservices.config.JwtService;
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
  private UserRepository repository;
  
  @Autowired
  private TokenRepository tokenRepository;
  
  @Autowired
  private PasswordEncoder passwordEncoder;
  
  @Autowired
  private JwtService jwtService;
  
  @Autowired
  private AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
	
    var user = User.builder()
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
        .createdDate((new Date()).getTime())
        .updatedDate((new Date()).getTime())
        .build();
  
    Optional<User> savedUser = repository.findByEmailIgnoreCase(user.getEmail());
   if(!savedUser.isPresent()) {
    	savedUser = Optional.ofNullable(repository.save(user));
    }
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser.get(), jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .firstname(savedUser.get().getFirstname())
        .lastname(savedUser.get().getLastname())
        .profileImageUrl(savedUser.get().getProfileImageUrl())
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
	 Optional<User> user =  repository.findByEmailIgnoreCase(request.getEmail());
	 User retrievedUser = user.get();
	 if (!passwordEncoder.matches(request.getPassword(), retrievedUser.getPassword())) {
				 throw new IllegalStateException("Wrong password");
	  }
			 
	  var jwtToken = jwtService.generateToken(retrievedUser);
	  var refreshToken = jwtService.generateRefreshToken(retrievedUser);
	  revokeAllUserTokens(retrievedUser);
	  saveUserToken(retrievedUser, jwtToken);
	  return AuthenticationResponse.builder()
					 .accessToken(jwtToken)
					 .refreshToken(refreshToken)
					 .firstname(retrievedUser.getFirstname())
					 .lastname(retrievedUser.getLastname())
					 .profileImageUrl(retrievedUser.getProfileImageUrl())
					 .build();
  }
  
  public AuthenticationResponse authenticateWithToken(
          HttpServletRequest request, AuthenticationRequest authenticationRequest){
		    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		    final String token;
		    final String userEmail = authenticationRequest.getEmail();
		    token = authHeader.substring(7);
	    var userNameInToken = jwtService.extractUsername(token);
	   
	    // Get the user based on the user in the token
	    var jwtToken = "";
    var refreshToken = "";
    User retrievedUser = null;
	   
	    if(userNameInToken.trim().toUpperCase().equals(userEmail.trim().toUpperCase())) {
	    	 Optional<User> savedUser = repository.findByEmailIgnoreCase(userEmail);
         retrievedUser = savedUser.get();
	    	jwtToken = jwtService.generateToken(retrievedUser);
	    	refreshToken = jwtService.generateRefreshToken(retrievedUser);
	    	revokeAllUserTokens(retrievedUser);
	    	saveUserToken(retrievedUser, jwtToken);
	    	} else {
	    		 throw new IllegalStateException("Wrong email address");
	    	}
	        return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .firstname(retrievedUser != null ? retrievedUser.getFirstname() : null)
        .lastname(retrievedUser != null ? retrievedUser.getLastname() : null)
        .profileImageUrl(retrievedUser != null ? retrievedUser.getProfileImageUrl() : null)
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
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmailIgnoreCase(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
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
	  if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
		  return "";
	  }
	  return authHeader.substring(7);
  }
}