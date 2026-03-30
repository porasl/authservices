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

  @Column(name = "token_sha256", insertable = false, updatable = false)
  private byte[] tokenSha256;

  public Token() {}

  public Token(Long id, String token, TokenType tokenType, boolean revoked, boolean expired, User user) {
    this.id = id;
    this.token = token;
    this.tokenType = tokenType;
    this.revoked = revoked;
    this.expired = expired;
    this.user = user;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }
  public TokenType getTokenType() { return tokenType; }
  public void setTokenType(TokenType tokenType) { this.tokenType = tokenType; }
  public boolean isExpired() { return expired; }
  public void setExpired(boolean expired) { this.expired = expired; }
  public boolean isRevoked() { return revoked; }
  public void setRevoked(boolean revoked) { this.revoked = revoked; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public byte[] getTokenSha256() { return tokenSha256; }

  public static TokenBuilder builder() {
    return new TokenBuilder();
  }

  public static class TokenBuilder {
    private String token;
    private TokenType tokenType = TokenType.BEARER;
    private boolean revoked;
    private boolean expired;
    private User user;

    public TokenBuilder token(String token) { this.token = token; return this; }
    public TokenBuilder tokenType(TokenType tokenType) { this.tokenType = tokenType; return this; }
    public TokenBuilder revoked(boolean revoked) { this.revoked = revoked; return this; }
    public TokenBuilder expired(boolean expired) { this.expired = expired; return this; }
    public TokenBuilder user(User user) { this.user = user; return this; }

    public Token build() {
      Token t = new Token();
      t.token = this.token;
      t.tokenType = this.tokenType;
      t.revoked = this.revoked;
      t.expired = this.expired;
      t.user = this.user;
      return t;
    }
  }
}