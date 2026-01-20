package com.porasl.authservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.service.JwtService;
import com.porasl.common.dto.TokenValidationResponse;

@RestController
@RequestMapping("/auth") // Or just "/auth" depending on your preference
public class AuthValidationController {

    @Autowired
    private JwtService jwtService; // The service that has your SECRET_KEY

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam("token") String token) {
        try {
            // 1. Extract the username (email) from the token
            String email = jwtService.extractUsername(token);

            // 2. Validate the token (check expiration and signature)
            // Assuming your JwtService has a method like isTokenValid or similar
            // If it's valid, return the data
            return ResponseEntity.ok(new TokenValidationResponse(true, email));
            
        } catch (Exception e) {
            // If token is expired, tampered with, or invalid
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }
    }
}