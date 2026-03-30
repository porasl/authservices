package com.porasl.authservices.token;

import com.porasl.authservices.user.Role;

public class RegisterRequest {

  private String firstname;
  private String lastname;
  private String email;
  private boolean status;
  private String password;
  private Role role;
  private String profileImageUrl;

  public RegisterRequest() {}

  public RegisterRequest(String firstname, String lastname, String email, boolean status, String password, Role role, String profileImageUrl) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.status = status;
    this.password = password;
    this.role = role;
    this.profileImageUrl = profileImageUrl;
  }

  public String getFirstname() { return firstname; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getLastname() { return lastname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }
  public boolean getStatus() { return status; }
  public void setStatus(boolean status) { this.status = status; }
  public String getProfileImageUrl() { return profileImageUrl; }
  public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

  public static RegisterRequestBuilder builder() {
    return new RegisterRequestBuilder();
  }

  public static class RegisterRequestBuilder {
    private String firstname;
    private String lastname;
    private String email;
    private boolean status;
    private String password;
    private Role role;

    public RegisterRequestBuilder firstname(String firstname) { this.firstname = firstname; return this; }
    public RegisterRequestBuilder lastname(String lastname) { this.lastname = lastname; return this; }
    public RegisterRequestBuilder email(String email) { this.email = email; return this; }
    public RegisterRequestBuilder password(String password) { this.password = password; return this; }
    public RegisterRequestBuilder role(Role role) { this.role = role; return this; }
    public RegisterRequestBuilder status(boolean status) { this.status = status; return this; }

    public RegisterRequest build() {
      RegisterRequest request = new RegisterRequest();
      request.firstname = this.firstname;
      request.lastname = this.lastname;
      request.email = this.email;
      request.password = this.password;
      request.role = this.role;
      request.status = this.status;
      return request;
    }
  }
}