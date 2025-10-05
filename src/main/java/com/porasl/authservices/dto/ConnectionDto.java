package com.porasl.authservices.dto;

import com.porasl.authservices.connection.UserConnection;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConnectionDto {
  private Long id;
  private Long requesterId;
  private Long targetId;
  private String status;
  private String note;          // <— NEW
  private long createdAt;
  private long updatedAt;
  private boolean created;      // true if newly created

  public static ConnectionDto of(UserConnection uc, boolean created) {
    return ConnectionDto.builder()
        .id(uc.getId())
        .requesterId(uc.getUserId())
        .targetId(uc.getTargetUserId())
        .status(uc.getStatus().name())
        .note(uc.getNote())
        .createdAt(uc.getCreatedAt() == null ? 0 : uc.getCreatedAt().toEpochMilli())
        .updatedAt(uc.getUpdatedAt() == null ? 0 : uc.getUpdatedAt().toEpochMilli())
        .created(created)
        .build();
  }
}
