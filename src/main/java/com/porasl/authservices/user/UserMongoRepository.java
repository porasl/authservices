package com.porasl.authservices.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMongoRepository extends JpaRepository<User, Integer> {
	 
	public User findByFirstname(String firstName);
	public List<User> findByLastname(String lastName);

  Optional<User> findByEmail(String email);

}
