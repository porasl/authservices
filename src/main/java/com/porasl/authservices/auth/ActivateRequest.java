package com.porasl.authservices.auth;

public class ActivateRequest {
  private String emailAddress;        
  private String activationCode;

  public ActivateRequest() {}

  public ActivateRequest(String emailAddress, String activationCode) {
    this.emailAddress = emailAddress;
    this.activationCode = activationCode;
  }

  public String getEmailAddress() { return emailAddress; }
  public String getActivationCode() { return activationCode; }

  public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
  public void setActivationCode(String activationCode) { this.activationCode = activationCode; }
}