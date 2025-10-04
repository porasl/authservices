package com.porasl.authservices.connection;


import com.porasl.authservices.user.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

Optional<UserConnection> findByUserAIdAndUserBId(Long a, Long b);

/** Connections where user is on the A side */
@Query("""
  select c.userB from UserConnection c
  where c.userA.id = :userId and c.status = com.porasl.authservices.connection.ConnectionStatus.ACCEPTED
""")
List<User> findAcceptedWhenUserIsA(@Param("userId") Long userId);

/** Connections where user is on the B side */
@Query("""
  select c.userA from UserConnection c
  where c.userB.id = :userId and c.status = com.porasl.authservices.connection.ConnectionStatus.ACCEPTED
""")
List<User> findAcceptedWhenUserIsB(@Param("userId") Long userId);
}

