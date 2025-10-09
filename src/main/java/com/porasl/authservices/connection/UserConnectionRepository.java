package com.porasl.authservices.connection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

Optional<UserConnection> findByUserIdAndTargetUserIdAndStatus(
   Long userId, Long targetUserId, UserConnection.Status status);

List<UserConnection> findByUserIdAndStatus(Long userId, UserConnection.Status status);
List<UserConnection> findByTargetUserIdAndStatus(Long targetUserId, UserConnection.Status status);

Optional<UserConnection> findBetweenUsers(long id, long id2);

//Replacement for "findBetweenUsers"
Optional<UserConnection> findByUserIdAndTargetUserId(Long userId, Long targetUserId);
Optional<UserConnection> findByTargetUserIdAndUserId(Long targetUserId, Long userId);

// (Optional) faster existence checks
boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);
boolean existsByTargetUserIdAndUserId(Long targetUserId, Long userId);
}
