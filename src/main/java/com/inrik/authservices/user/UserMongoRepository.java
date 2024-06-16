package com.inrik.authservices.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMongoRepository extends JpaRepository<User, Integer> {
	 
	public User findByFirstName(String firstName);
	public List<User> findByLastName(String lastName);

  Optional<User> findByEmail(String email);

}
