package com.porasl.authservices.controller;

import com.porasl.authservices.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    UserProfileResponse response = new UserProfileResponse(
                        String.valueOf(user.getId()),
                        user.getEmail(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getProfileImageUrl()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    public static class UserProfileResponse {
        private final String id;
        private final String email;
        private final String firstname;
        private final String lastname;
        private final String profileImageUrl;

        public UserProfileResponse(String id, String email, String firstname, String lastname, String profileImageUrl) {
            this.id = id;
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
            this.profileImageUrl = profileImageUrl;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getFirstname() { return firstname; }
        public String getLastname() { return lastname; }
        public String getProfileImageUrl() { return profileImageUrl; }
    }
}
