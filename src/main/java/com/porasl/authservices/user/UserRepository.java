package com.porasl.authservices.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.friends WHERE u.id = :userId")
    Optional<User> findByIdWithFriends(@Param("userId") Long userId);

}
