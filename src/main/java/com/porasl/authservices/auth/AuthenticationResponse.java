package com.porasl.authservices.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationResponse {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  private String refreshToken;

  private String firstname;
  private String lastname;
  private String profileImageUrl;

  public AuthenticationResponse() {}

  public AuthenticationResponse(String accessToken, String refreshToken, String firstname, String lastname, String profileImageUrl) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.firstname = firstname;
    this.lastname = lastname;
    this.profileImageUrl = profileImageUrl;
  }

  public String getAccessToken() { return accessToken; }
  public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
  public String getRefreshToken() { return refreshToken; }
  public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
  public String getFirstname() { return firstname; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getLastname() { return lastname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getProfileImageUrl() { return profileImageUrl; }
  public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

  public static AuthenticationResponseBuilder builder() {
    return new AuthenticationResponseBuilder();
  }

  public static class AuthenticationResponseBuilder {
    private String accessToken;
    private String refreshToken;
    private String firstname;
    private String lastname;
    private String profileImageUrl;

    public AuthenticationResponseBuilder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public AuthenticationResponseBuilder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public AuthenticationResponseBuilder firstname(String firstname) {
      this.firstname = firstname;
      return this;
    }

    public AuthenticationResponseBuilder lastname(String lastname) {
      this.lastname = lastname;
      return this;
    }

    public AuthenticationResponseBuilder profileImageUrl(String profileImageUrl) {
      this.profileImageUrl = profileImageUrl;
      return this;
    }

    public AuthenticationResponse build() {
      AuthenticationResponse response = new AuthenticationResponse();
      response.accessToken = this.accessToken;
      response.refreshToken = this.refreshToken;
      response.firstname = this.firstname;
      response.lastname = this.lastname;
      response.profileImageUrl = this.profileImageUrl;
      return response;
    }
  }
}