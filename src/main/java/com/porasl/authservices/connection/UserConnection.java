package com.porasl.authservices.connection;

import com.porasl.authservices.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
name = "user_connection",
uniqueConstraints = @UniqueConstraint(name = "uq_connection_pair", columnNames = {"user_id_a", "user_id_b"}),
indexes = {
 @Index(name = "idx_user_a", columnList = "user_id_a"),
 @Index(name = "idx_user_b", columnList = "user_id_b"),
 @Index(name = "idx_status", columnList = "status")
}
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserConnection {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

/** Always the smaller user id goes into userA, larger into userB (canonical order) */
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id_a", nullable = false,
 foreignKey = @ForeignKey(name = "fk_connection_user_a"))
private User userA;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id_b", nullable = false,
 foreignKey = @ForeignKey(name = "fk_connection_user_b"))
private User userB;

@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 16)
private ConnectionStatus status = ConnectionStatus.ACCEPTED;

@Column(nullable = false, updatable = false)
private Instant createdAt;

@Column(nullable = false)
private Instant updatedAt;

@PrePersist
void onCreate() {
 final Instant now = Instant.now();
 createdAt = now;
 updatedAt = now;
}

@PreUpdate
void onUpdate() {
 updatedAt = Instant.now();
}
}
