package com.porasl.authservices.dto;

import com.porasl.authservices.connection.UserConnection;

public class ConnectionDto {
  private Long id;
  private Long user_id;
  private Long target_user_id;
  private String status;
  private String note;         
  private long createdAt;
  private long updatedAt;
  private boolean created;      

  public ConnectionDto() {}

  public ConnectionDto(Long id, Long user_id, Long target_user_id, String status, String note, long createdAt, long updatedAt, boolean created) {
    this.id = id;
    this.user_id = user_id;
    this.target_user_id = target_user_id;
    this.status = status;
    this.note = note;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.created = created;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUser_id() { return user_id; }
  public void setUser_id(Long user_id) { this.user_id = user_id; }
  public Long getTarget_user_id() { return target_user_id; }
  public void setTarget_user_id(Long target_user_id) { this.target_user_id = target_user_id; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public long getCreatedAt() { return createdAt; }
  public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
  public long getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
  public boolean isCreated() { return created; }
  public void setCreated(boolean created) { this.created = created; }

  public static ConnectionDtoBuilder builder() {
    return new ConnectionDtoBuilder();
  }

  public static class ConnectionDtoBuilder {
    private Long id;
    private Long user_id;
    private Long target_user_id;
    private String status;
    private String note;
    private long createdAt;
    private long updatedAt;
    private boolean created;

    public ConnectionDtoBuilder id(Long id) { this.id = id; return this; }
    public ConnectionDtoBuilder user_id(Long user_id) { this.user_id = user_id; return this; }
    public ConnectionDtoBuilder target_user_id(Long target_user_id) { this.target_user_id = target_user_id; return this; }
    public ConnectionDtoBuilder status(String status) { this.status = status; return this; }
    public ConnectionDtoBuilder note(String note) { this.note = note; return this; }
    public ConnectionDtoBuilder createdAt(long createdAt) { this.createdAt = createdAt; return this; }
    public ConnectionDtoBuilder updatedAt(long updatedAt) { this.updatedAt = updatedAt; return this; }
    public ConnectionDtoBuilder created(boolean created) { this.created = created; return this; }

    public ConnectionDto build() {
      return new ConnectionDto(id, user_id, target_user_id, status, note, createdAt, updatedAt, created);
    }
  }

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