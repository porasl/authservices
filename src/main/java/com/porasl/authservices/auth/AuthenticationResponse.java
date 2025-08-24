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

  // Manual getters and builder to ensure compilation
  public String getAccessToken() { return accessToken; }
  public String getRefreshToken() { return refreshToken; }

  public static AuthenticationResponseBuilder builder() {
    return new AuthenticationResponseBuilder();
  }

  public static class AuthenticationResponseBuilder {
    private String accessToken;
    private String refreshToken;

    public AuthenticationResponseBuilder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public AuthenticationResponseBuilder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public AuthenticationResponse build() {
      AuthenticationResponse response = new AuthenticationResponse();
      response.accessToken = this.accessToken;
      response.refreshToken = this.refreshToken;
      return response;
    }
  }
}