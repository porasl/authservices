package com.porasl.authservices.connection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.porasl.authservices.connection.UserConnection.Status;
import com.porasl.authservices.connection.model.ConnectionStatus;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

  Optional<UserConnection> findByUserIdAndTargetUserIdAndStatus(
      Long userId, Long targetUserId, UserConnection.Status status);

  List<UserConnection> findByUserIdAndStatus(Long userId, UserConnection.Status status);

  List<UserConnection> findByTargetUserIdAndStatus(Long targetUserId, UserConnection.Status status);

  // Replacement for "findBetweenUsers" when checking one direction
  Optional<UserConnection> findByUserIdAndTargetUserId(Long userId, Long targetUserId);
  Optional<UserConnection> findByTargetUserIdAndUserId(Long targetUserId, Long userId);

  // (Optional) faster existence checks
  boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);
  boolean existsByTargetUserIdAndUserId(Long targetUserId, Long userId);

  // ✅ Single JPQL method to match either direction (A→B or B→A)
  @Query("""
      select uc from UserConnection uc
      where (uc.userId = :a and uc.targetUserId = :b)
         or (uc.userId = :b and uc.targetUserId = :a)
      """)
  Optional<UserConnection> findBetweenUsers(@Param("a") Long a, @Param("b") Long b);
  
  
  // Return the "other side" user for all ACCEPTED connections involving :userId
  @Query("""
    select case
             when uc.requester.id = :userId then uc.target
             else uc.requester
           end
    from UserConnection uc
    where (uc.requester.id = :userId or uc.target.id = :userId)
      and uc.status = :status
  """)
  List<com.porasl.authservices.user.User> findAcceptedCounterparties(
      @Param("userId") Long userId,
      @Param("status") Status accepted);

}
