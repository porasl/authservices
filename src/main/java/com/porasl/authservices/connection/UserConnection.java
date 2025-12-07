package com.porasl.authservices.connection;

import java.time.Instant;

import com.porasl.authservices.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_connections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "target_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who initiated the request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** The user receiving the request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private Status status;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum Status { PENDING, ACCEPTED, BLOCKED }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

	public void setTargetUserId(long id2) {
		// TODO Auto-generated method stub
		
	}
}
