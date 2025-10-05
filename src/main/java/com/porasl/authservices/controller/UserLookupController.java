package com.porasl.authservices.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.dto.UserDTO;
import com.porasl.authservices.user.UserRepository;

@RestController
@RequestMapping("/internal/users")
class UserLookupController {
    private final UserRepository repo;

    public UserLookupController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping        // GET /internal/users?emails=a@x&emails=b@y
    List<UserDTO> byEmails(@RequestParam String email) {
        return repo.findByEmailIgnoreCase(email).stream()
            .map(u -> new UserDTO(u.getId(), u.getEmail(), u.getFirstname(), u.getLastname(), u.getProfileImageUrl()))
            .toList();
    }
}


