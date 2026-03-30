package com.porasl.authservices.user;

public class DeleteUserRequest {

  private String email;
  private String password;

  public DeleteUserRequest() {}

  public DeleteUserRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}