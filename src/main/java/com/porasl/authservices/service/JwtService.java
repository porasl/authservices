package com.porasl.authservices.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.porasl.authservices.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;          // new constants live here
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * Unified JWT service:
 * - HS256 mode if 'application.security.jwt.secret-key' is provided (legacy).
 * - RS256 mode if private/public key locations are provided.
 *
 * Validates issuer/audience when configured. Supports both "old" millisecond
 * expiration properties and the "new" minutes/days style.
 */
@Service
public class JwtService {

  /* ---------- Legacy HS256 props (kept for backward compatibility) ---------- */
  @Value("${application.security.jwt.secret-key:}")
  private String secretKeyBase64;

  @Value("${application.security.jwt.expiration:0}")                 // milliseconds
  private long jwtExpirationMs;

  @Value("${application.security.jwt.refresh-token.expiration:0}")   // milliseconds
  private long refreshExpirationMs;

  /* ---------- RS256 props (recommended) ---------- */
  @Value("${application.security.jwt.issuer:}")
  private String issuer;

  // Comma-separated or single string (e.g. "inrik-storefront,inrik-mobile")
  @Value("${application.security.jwt.audience:}")
  private String audienceCsv;

  @Value("${application.security.jwt.access-minutes:0}")    // minutes
  private long accessMinutes;

  @Value("${application.security.jwt.refresh-days:0}")      // days
  private long refreshDays;

  @Value("${application.security.jwt.clock-skew-seconds:60}")
  private long clockSkewSeconds;

  // e.g. file:/path/to/jwt-private.pem or classpath:jwt-private.pem
  @Value("${application.security.jwt.private-key-location:}")
  private String privateKeyLocation;

  @Value("${application.security.jwt.public-key-location:}")
  private String publicKeyLocation;

  /* ---------- Resolved runtime state ---------- */
  private SecretKey hmacKey;   // NEW (HMAC key must be SecretKey)
  private PrivateKey rsaPrivateKey;    // RS256
  private PublicKey rsaPublicKey;      // RS256
  private List<String> audiences = List.of();
  private boolean hsMode = false;      // true = HS256, false = RS256

  @PostConstruct
  void init() {
    // Parse audiences (allow comma-separated list)
    if (audienceCsv != null && !audienceCsv.isBlank()) {
      audiences = Arrays.stream(audienceCsv.split(","))
          .map(String::trim)
          .filter(s -> !s.isBlank())
          .collect(Collectors.toList());
    }

    // Prefer HS256 if secret-key is present (backward-compatible)
    if (secretKeyBase64 != null && !secretKeyBase64.isBlank()) {
      byte[] keyBytes = Decoders.BASE64.decode(secretKeyBase64);
      hmacKey = Keys.hmacShaKeyFor(keyBytes);
      hsMode = true;
      return;
    }

    // Else try RS256 if key locations are provided
    if (isText(privateKeyLocation) && isText(publicKeyLocation)) {
      rsaPrivateKey = readPrivateKeyFromPem(privateKeyLocation);
      rsaPublicKey  = readPublicKeyFromPem(publicKeyLocation);
      hsMode = false;
      return;
    }

    // If neither configured, fail fast
    throw new IllegalStateException(
        "JWT configuration missing. Provide either 'application.security.jwt.secret-key' (HS256) "
      + "or both 'application.security.jwt.private-key-location' and '...public-key-location' (RS256).");
  }

  /* ========================= Public API ========================= */

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> resolver) {
    final Claims claims = extractAllClaims(token);
    return resolver.apply(claims);
  }

  public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails user) {
    String username = extractUsername(token);
    return username.equalsIgnoreCase(user.getUsername()) && !isTokenExpired(token) && validateIssuerAudience(token);
  }

  public boolean isTokenValid(String token) {
    return !isTokenExpired(token) && validateIssuerAudience(token);
  }

  public String generateToken(org.springframework.security.core.userdetails.UserDetails user) {
    return generateToken(Collections.emptyMap(), user);
  }

  public String generateToken(Map<String, Object> extraClaims, org.springframework.security.core.userdetails.UserDetails user) {
    return buildToken(extraClaims, user.getUsername(), false);
  }

  public String generateRefreshToken(org.springframework.security.core.userdetails.UserDetails user) {
    return buildToken(Collections.emptyMap(), user.getUsername(), true);
  }

  /* ========================= Internals ========================= */

  private String buildToken(Map<String, Object> extraClaims, String subject, boolean refresh) {
    final long nowMs = System.currentTimeMillis();
    final Date iat = new Date(nowMs);
    final Date exp = new Date(nowMs + resolveExpiryMillis(refresh));

    Map<String, Object> claims = new HashMap<>(extraClaims);
    if (isText(issuer)) claims.putIfAbsent("iss", issuer);
    if (!audiences.isEmpty()) {
      // RFC7519 allows 'aud' to be String or array. Use array if >1.
      claims.putIfAbsent("aud", audiences.size() == 1 ? audiences.get(0) : audiences);
    }

    JwtBuilder builder = Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(iat)
        .setExpiration(exp);

    if (hsMode) {
    	  return builder
    	      .signWith(hmacKey, Jwts.SIG.HS256)   // <— NEW
    	      .compact();
    	} else {
    	  return builder
    	      .signWith(rsaPrivateKey, Jwts.SIG.RS256)  // <— NEW
    	      .compact();
    	}
  }

  private boolean validateIssuerAudience(String token) {
    Claims c = extractAllClaims(token);

    // Issuer check (optional)
    if (isText(issuer)) {
      String iss = c.getIssuer();
      if (iss == null || !iss.equals(issuer)) return false;
    }

    // Audience check (optional)
    if (!audiences.isEmpty()) {
      Object audClaim = c.get("aud");
      if (audClaim == null) return false;

      if (audClaim instanceof String s) {
        return audiences.contains(s);
      } else if (audClaim instanceof Collection<?> col) {
        // Any overlap is acceptable
        for (Object o : col) {
          if (o != null && audiences.contains(String.valueOf(o))) return true;
        }
        return false;
      } else {
        // Unknown type
        return false;
      }
    }
    return true; // no issuer/audience constraints configured
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
	  JwtParserBuilder parser = Jwts.parser()
			  .clockSkewSeconds(clockSkewSeconds);	  
	     // .clockSkew(Duration.ofSeconds(clockSkewSeconds));

	  if (hsMode) {
	    parser = parser.verifyWith(hmacKey);         // SecretKey
	  } else {
	    parser = parser.verifyWith(rsaPublicKey);    // PublicKey
	  }

	  return parser.build().parseSignedClaims(token).getPayload();
	}



  private long resolveExpiryMillis(boolean refresh) {
    // 1) Legacy HS256 durations (ms) if configured
    if (hsMode) {
      if (refresh && refreshExpirationMs > 0) return refreshExpirationMs;
      if (!refresh && jwtExpirationMs > 0)   return jwtExpirationMs;
      // Fallback sane defaults
      return refresh ? Duration.ofDays(14).toMillis() : Duration.ofMinutes(15).toMillis();
    }

    // 2) RS256 durations (minutes/days)
    if (refresh && refreshDays > 0)   return Duration.ofDays(refreshDays).toMillis();
    if (!refresh && accessMinutes > 0) return Duration.ofMinutes(accessMinutes).toMillis();

    // Fallback
    return refresh ? Duration.ofDays(14).toMillis() : Duration.ofMinutes(15).toMillis();
  }

  /* ========================= Key loading helpers ========================= */

  private static boolean isText(String s) {
    return s != null && !s.isBlank();
  }

  private static String readTextResource(String location) {
    try {
      // Allow plain paths ("~/.../file.pem") by prefixing file:
      String loc = (location.startsWith("classpath:") || location.startsWith("file:"))
          ? location
          : "file:" + location;
      try (InputStream in = ResourceUtils.getURL(loc).openStream()) {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read PEM from " + location, e);
    }
  }

  private static PrivateKey readPrivateKeyFromPem(String location) {
    try {
      String pem = readTextResource(location);
      String base64 = pem
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s", "");
      byte[] der = Base64.getDecoder().decode(base64);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
      return KeyFactory.getInstance("RSA").generatePrivate(spec);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse RSA private key: " + location, e);
    }
  }

  private static PublicKey readPublicKeyFromPem(String location) {
    try {
      String pem = readTextResource(location);
      String base64 = pem
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");
      byte[] der = Base64.getDecoder().decode(base64);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
      return KeyFactory.getInstance("RSA").generatePublic(spec);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse RSA public key: " + location, e);
    }
  }
  
  public String generateAccessToken(User user) {
	  Map<String, Object> claims = new HashMap<>();
	  claims.put("uid", user.getId());
	  claims.put("given_name", user.getFirstname());
	  claims.put("family_name", user.getLastname());
	  claims.put("name", (user.getFirstname() == null ? "" : user.getFirstname()) +
	                     (user.getLastname() == null ? "" : " " + user.getLastname()));
	  claims.put("role", user.getRole() != null ? user.getRole().name() : "USER");
	  if (user.getProfileImageUrl() != null) {
	    claims.put("picture", user.getProfileImageUrl());
	  }
	  // Optional (redundant with sub, but many clients like it):
	  claims.put("email", user.getEmail());

	  return generateToken(claims, user);
	}

	public String generateRefreshToken(User user) {
	  // usually smaller; you can also include the same claims if your clients need them on refresh
	  return generateRefreshToken((UserDetails) user);
	}
	
}
