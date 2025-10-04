package com.porasl.authservices.dto;


import com.porasl.authservices.connection.model.ConnectionStatus;
import lombok.*;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class ConnectionDto {
private Long id;              // PK of the connection row
private Long requesterId;     // who initiated
private Long targetId;        // who is being connected to
private ConnectionStatus status; // PENDING | ACCEPTED | BLOCKED
private long createdAt;       // epoch millis
private long updatedAt;       // epoch millis

/**
* True when this API call actually created the edge (or auto-accepted
* a reverse pending). Useful for returning 201 vs 200.
*/
private boolean created;
}

