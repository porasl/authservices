package com.porasl.authservices.token;

import com.porasl.authservices.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token", nullable = false, columnDefinition = "TEXT")
  private String token;

  @Enumerated(EnumType.STRING)
  private TokenType tokenType = TokenType.BEARER;

  private boolean revoked;

  private boolean expired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  
//Optional: map the hash column (read-only)
 @Column(name = "token_sha256", insertable = false, updatable = false)
 private byte[] tokenSha256;

  // Manual getters/setters to ensure compilation
  public boolean isExpired() { return expired; }
  public boolean isRevoked() { return revoked; }
  public void setExpired(boolean expired) { this.expired = expired; }
  public void setRevoked(boolean revoked) { this.revoked = revoked; }

  // Manual builder method
  public static TokenBuilder builder() {
    return new TokenBuilder();
  }

  public static class TokenBuilder {
    private String token;
    private TokenType tokenType = TokenType.BEARER;
    private boolean revoked;
    private boolean expired;
    private User user;

    public TokenBuilder token(String token) {
      this.token = token;
      return this;
    }

    public TokenBuilder tokenType(TokenType tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public TokenBuilder revoked(boolean revoked) {
      this.revoked = revoked;
      return this;
    }

    public TokenBuilder expired(boolean expired) {
      this.expired = expired;
      return this;
    }

    public TokenBuilder user(User user) {
      this.user = user;
      return this;
    }

    public Token build() {
      Token token = new Token();
      token.token = this.token;
      token.tokenType = this.tokenType;
      token.revoked = this.revoked;
      token.expired = this.expired;
      token.user = this.user;
      return token;
    }
  }
}