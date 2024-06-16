package com.inrik.authservices.user;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
    private PasswordEncoder passwordEncoder;
	
	
	@Autowired
    private  UserRepository repository;
    
    public void changePasswordByUser(String userId, ChangePasswordRequest request) {
    	//Get the user by email
        Optional<User> user =  repository.findByEmail(userId);
         User retrievedUser = user.get();
        if(user.isPresent()) {
        	if (!passwordEncoder.matches(request.getCurrentPassword(), retrievedUser.getPassword())) {
        		throw new IllegalStateException("Wrong password");
        	}
        	if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
        		throw new IllegalStateException("Password are not the same");
        	}
        	retrievedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        	repository.save(retrievedUser);
        }
    }
    
    public void changePasswordByAdmin(ChangePasswordRequest request, String adminUserId) {
    	//Make sure userId belongs to an Admin
    	
    	 Optional<User> userAdmin =  repository.findByEmail(adminUserId);
    	 String role = userAdmin.get().getRole().name();
    	 boolean blocked = userAdmin.get().getBlocked();
    	 boolean active = userAdmin.get().getStatus();
    	 
    	 
    	//Get the user by email
        Optional<User> user =  repository.findByEmail(request.getUserEmail());
    	 
         User retrievedUser = user.get();
        if(user.isPresent()) {
        	if (!passwordEncoder.matches(request.getCurrentPassword(), retrievedUser.getPassword())) {
        		throw new IllegalStateException("Wrong password");
        	}
        	if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
        		throw new IllegalStateException("Password are not the same");
        	}
        	retrievedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        	repository.save(retrievedUser);
        }
    }
    
    public void deleteUser(DeleteUserRequest request, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        repository.delete(user);
    }
}