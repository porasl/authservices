package com.porasl.authservices.user;

import java.security.Principal;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.porasl.authservices.auth.ActivateRequest;
import com.porasl.authservices.connection.UserConnectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

	@Slf4j
	@Service
	@RequiredArgsConstructor
	public class UserService {

	    private final UserRepository userRepository;
	    private final UserConnectionRepository connectionRepository;
	    private final PasswordEncoder passwordEncoder; 

	    @Transactional
	    public void deleteUserAndConnections(Principal connectedUser) {
	        if (connectedUser == null) {
	            log.warn("Attempted delete with null principal");
	            throw new IllegalStateException("No authenticated user");
	        }

	        String name = connectedUser.getName();
	        log.info("Deleting user and all connections for principal: {}", name);

	        User user = userRepository.findByEmailIgnoreCase(name)
	                .or(() -> userRepository.findByEmailIgnoreCase(name))
	                .orElseThrow(() -> {
	                    log.error("User not found for principal: {}", name);
	                    return new IllegalStateException("User not found for principal: " + name);
	                });

	        Long id = user.getId();
	        log.debug("Found user ID {}. Deleting related connections...", id);

	        connectionRepository.deleteByRequesterIdOrTargetId(id, id);
	        log.info("Deleted all connections for user ID {}", id);

	        userRepository.deleteById(id);
	        log.info("Deleted user ID {}", id);
	    }

	
	 /** Case-insensitive email lookup. */
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    /** Case-insensitive username lookup (requires repo support). */
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        // If you don't have this repository method yet, add it:
        // Optional<User> findByUsernameIgnoreCase(String username);
        return userRepository.findByEmailIgnoreCase(username.trim());
    }

    
    public void changePasswordByUser(ChangePasswordRequest request) {
    	//Get the user by email
        Optional<User> user =  userRepository.findByEmailIgnoreCase(request.getUserEmail());
         User retrievedUser = user.get();
        if(user.isPresent()) {
        	if (!passwordEncoder.matches(request.getCurrentPassword(), retrievedUser.getPassword())) {
        		throw new IllegalStateException("Wrong password");
        	}
        	if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
        		throw new IllegalStateException("Password are not the same");
        	}
        	retrievedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        	userRepository.save(retrievedUser);
        }
    }
    
    public void changePasswordByAdmin(ChangePasswordRequest request, String adminUserId) {
    	//Make sure userId belongs to an Admin
    	
    	 Optional<User> userAdmin =  userRepository.findByEmailIgnoreCase(adminUserId);
    	 String role = userAdmin.get().getRole().name();
    	 boolean blocked = userAdmin.get().isBlocked();
    	 boolean active = userAdmin.get().getStatus();
    	 if (role.equalsIgnoreCase("ADMIN") && !blocked && active) {
    		 //Get the user by email
    		 Optional<User> user =  userRepository.findByEmailIgnoreCase(request.getUserEmail());
    		 User retrievedUser = user.get();
    		 if(user.isPresent()) {
    			 if (!passwordEncoder.matches(request.getCurrentPassword(), retrievedUser.getPassword())) {
    				 throw new IllegalStateException("Wrong password");
    			 }
    			 if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
        		throw new IllegalStateException("Password are not the same");
    			 }
    			 retrievedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
    			 userRepository.save(retrievedUser);
    		 }
        }
    }
    
    public Boolean activate(ActivateRequest request) {
    	
    	// Get the user from Request
    	var emailAddress = request.getEmailAddress();
    	var activationCode = request.getActivationCode();
    	Optional<User> user =  userRepository.findByEmailIgnoreCase(emailAddress);
		 if(user.isPresent()) {
			 User exitingUser = user.get();
			 if(exitingUser.getActivationcode().equals(activationCode)) {
				 exitingUser.setApproved(true);
				 exitingUser.setStatus(true);
			 }
			 userRepository.save(exitingUser);
			 return true;
		 }
		 return false;
    }
    
    public void deleteUser(DeleteUserRequest request, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        userRepository.delete(user);
    }
}