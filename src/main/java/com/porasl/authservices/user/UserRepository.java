package com.porasl.authservices.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);

    // Option 1: EntityGraph-based eager load of both connection lists
    @EntityGraph(attributePaths = {"sentConnections", "receivedConnections"})
    Optional<User> findById(Long id);

    // Option 2 (optional): keep JPQL version for explicit fetching
    @Query("""
      SELECT DISTINCT u 
      FROM User u
      LEFT JOIN FETCH u.sentConnections sc
      LEFT JOIN FETCH u.receivedConnections rc
      WHERE u.id = :userId
      """)
    Optional<User> findByIdWithFriends(@Param("userId") Long userId);
    
    //@EntityGraph(attributePaths = {"sentConnections", "receivedConnections"})
   // @Query("SELECT u FROM User u")
    //List<User> getAllUsers();
    
    

}