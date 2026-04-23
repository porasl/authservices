package com.porasl.authservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ConnectionRequestDto {
    @NotBlank
    @Email
    private String targetEmail;
    
    private String notes;
    
    private boolean autoAccept = false;

    public ConnectionRequestDto() {}

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isAutoAccept() { return autoAccept; }
    public void setAutoAccept(boolean autoAccept) { this.autoAccept = autoAccept; }
}