package com.porasl.authservices.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  private String refreshToken;

  // Additional user info for frontend convenience
  private String firstname;
  private String lastname;
  private String profileImageUrl;

  // Manual getters and builder to ensure compilation
  public String getAccessToken() { return accessToken; }
  public String getRefreshToken() { return refreshToken; }

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