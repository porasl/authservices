package com.porasl.authservices.connection;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

//package com.porasl.authservices.connection;

@Entity
@Table(
name = "user_connections",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_user_id"})
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserConnection {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(name = "user_id", nullable = false)
private Long userId;

@Column(name = "target_user_id", nullable = false)
private Long targetUserId;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 16)
private Status status;

//@Enumerated(EnumType.STRING)
//private ConnectionStatus status = ConnectionStatus.PENDING;

@Column(name = "note", length = 255)         // <— NEW (nullable OK)
private String note;

@Column(name = "created_by", nullable = false)
private Long createdBy;

@Column(name = "created_at", nullable = false)
private Instant createdAt;

@Column(name = "updated_at", nullable = false)
private Instant updatedAt;

public enum Status { PENDING, ACCEPTED, BLOCKED }

@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="requester_id")
private com.porasl.authservices.user.User requester;

@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="target_id")
private com.porasl.authservices.user.User target;



public void touchCreated() {
	// TODO Auto-generated method stub
	
}
}
