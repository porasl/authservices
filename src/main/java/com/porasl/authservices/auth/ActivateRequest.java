package com.porasl.authservices.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivateRequest {
  private String emailAddress;        
  private String activationCode;

  // Manual getters to ensure compilation
  public String getEmailAddress() { return emailAddress; }
  public String getActivationCode() { return activationCode; }
  
  // Manual setters
  public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
  public void setActivationCode(String activationCode) { this.activationCode = activationCode; }
}