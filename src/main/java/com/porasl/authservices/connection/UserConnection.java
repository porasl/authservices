package com.porasl.authservices.connection;

import java.time.Instant;

import com.porasl.authservices.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_connections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_user_id"})
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
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    /** The user receiving the request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User target;

    @ManyToOne
    private User user;
    
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
}
