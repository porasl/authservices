package com.porasl.authservices.dto;

import com.porasl.authservices.connection.UserConnection;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConnectionDto {
  private Long id;
  private Long user_id;
  private Long target_user_id;
  private String status;
  private String note;         
  private long createdAt;
  private long updatedAt;
  private boolean created;      

  public static ConnectionDto of(UserConnection uc, boolean created) {
    return ConnectionDto.builder()
        .id(uc.getId())
        .target_user_id(uc.getTarget().getId())
        .user_id(uc.getRequester().getId())
        .status(uc.getStatus().name())
        .note(uc.getNote())
        .createdAt(uc.getCreatedAt() == null ? 0 : uc.getCreatedAt().toEpochMilli())
        .updatedAt(uc.getUpdatedAt() == null ? 0 : uc.getUpdatedAt().toEpochMilli())
        .created(created)
        .build();
  }
}
