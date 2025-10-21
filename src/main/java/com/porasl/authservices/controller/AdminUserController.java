package com.porasl.authservices.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.user.UserRepository;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

  private final UserRepository userRepository;

  public AdminUserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @DeleteMapping("/by-email/{email:.+}")
  @PreAuthorize("hasRole('ADMIN')") // requires your JWT->GrantedAuthority mapping to set ROLE_ADMIN
  public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
    if (!userRepository.existsByEmailIgnoreCase(email)) {
      return ResponseEntity.notFound().build();
    }
    userRepository.deleteByEmailIgnoreCase(email);
    return ResponseEntity.noContent().build();
  }
}
