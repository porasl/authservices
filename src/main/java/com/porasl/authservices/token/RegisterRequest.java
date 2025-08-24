package com.porasl.authservices.token;

import com.porasl.authservices.user.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  private String firstname;
  private String lastname;
  private String email;
  private boolean status;
  private String password;
  private Role role;
  private String profileImageUrl;

  // Manual getters to ensure compilation
  public String getFirstname() { return firstname; }
  public String getLastname() { return lastname; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }
  public Role getRole() { return role; }
  public boolean getStatus() { return status; }
  public String getProfileImageUrl() { return profileImageUrl; }

  // Manual builder method
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

    public RegisterRequestBuilder firstname(String firstname) {
      this.firstname = firstname;
      return this;
    }

    public RegisterRequestBuilder lastname(String lastname) {
      this.lastname = lastname;
      return this;
    }

    public RegisterRequestBuilder email(String email) {
      this.email = email;
      return this;
    }

    public RegisterRequestBuilder password(String password) {
      this.password = password;
      return this;
    }

    public RegisterRequestBuilder role(Role role) {
      this.role = role;
      return this;
    }

    public RegisterRequestBuilder status(boolean status) {
      this.status = status;
      return this;
    }

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