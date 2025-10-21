package com.porasl.authservices.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	 Optional<User> findByEmailIgnoreCase(String email);
	  boolean existsByEmailIgnoreCase(String email);
	  void deleteByEmailIgnoreCase(String email);

}
