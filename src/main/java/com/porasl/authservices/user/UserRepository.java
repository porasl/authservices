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

    // NEW — required for your controller
    List<User> findAllByEmailInIgnoreCase(List<String> emails);

    // Load both lists for a single user
    @EntityGraph(attributePaths = {"sentConnections", "receivedConnections"})
    Optional<User> findById(Long id);

    @Query("""
      SELECT DISTINCT u 
      FROM User u
      LEFT JOIN FETCH u.sentConnections sc
      LEFT JOIN FETCH u.receivedConnections rc
      WHERE u.id = :userId
      """)
    Optional<User> findByIdWithFriends(@Param("userId") Long userId);

    Optional<User> findByEmailIgnoreCaseAndIsPlaceholderTrue(String email);

    Optional<User> findByEmailIgnoreCaseAndIsPlaceholderFalse(String email);

}
