package com.porasl.authservices.connection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.porasl.authservices.connection.UserConnection.Status;
import com.porasl.authservices.user.User;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

    // ==========================================================
    // BASIC LOOKUPS (new JPA relationship names)
    // ==========================================================

    Optional<UserConnection> findByRequesterIdAndTargetId(Long requesterId, Long targetId);

    Optional<UserConnection> findByRequesterIdAndTargetIdAndStatus(
            Long requesterId, Long targetId, Status status);

    List<UserConnection> findByRequesterIdAndStatus(Long requesterId, Status status);

    List<UserConnection> findByTargetIdAndStatus(Long targetId, Status status);

    // Checks in reverse direction
    Optional<UserConnection> findByTargetIdAndRequesterId(Long targetId, Long requesterId);

    boolean existsByRequesterIdAndTargetId(Long requesterId, Long targetId);

    boolean existsByTargetIdAndRequesterId(Long targetId, Long requesterId);


    // ==========================================================
    // MATCH CONNECTION BETWEEN TWO USERS (bidirectional)
    // ==========================================================

    @Query("""
        select uc
        from UserConnection uc
        where (uc.requester.id = :a and uc.target.id = :b)
           or (uc.requester.id = :b and uc.target.id = :a)
        """)
    Optional<UserConnection> findBetweenUsers(
            @Param("a") Long a,
            @Param("b") Long b);


    // ==========================================================
    // GET ACCEPTED FRIENDS (returns the opposite user)
    // ==========================================================


    // ==========================================================
    // DELETE / FIND all connections where user is requester or target
    // ==========================================================

    @Transactional
    void deleteByRequesterIdOrTargetId(Long requesterId, Long targetId);

    List<UserConnection> findByRequesterIdOrTargetId(Long requesterId, Long targetId);
    @Query("""
    	    select uc.target
    	    from UserConnection uc
    	    where uc.requester.id = :userId
    	      and uc.status = :status
    	""")
    	List<User> findAcceptedTargets(@Param("userId") Long userId,
    	                               @Param("status") Status status);

    	@Query("""
    	    select uc.requester
    	    from UserConnection uc
    	    where uc.target.id = :userId
    	      and uc.status = :status
    	""")
    	List<User> findAcceptedRequesters(@Param("userId") Long userId,
    	                                  @Param("status") Status status);


    // ==========================================================
    // FIND USERS WITH ANY STATUS (generic)
    // ==========================================================

    @Query("""
        select case
                 when c.requester.id = :userId then c.target
                 else c.requester
               end
        from UserConnection c
        where (c.requester.id = :userId or c.target.id = :userId)
          and c.status = :status
        """)
    List<User> findCounterpartiesByStatus(
            @Param("userId") Long userId,
            @Param("status") Status status);

    
	Object findByUserIdAndTargetUserId(long requesterId, long id);

	User findByUserIdAndTargetUserIdAndStatus(long id, long requesterId, Status pending);
}
