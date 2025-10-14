package com.porasl.authservices.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

//JwtProps.java
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProps {
private String issuer;
private List<String> audience = List.of();
private int accessMinutes = 30;
private int refreshDays = 30;
private int clockSkewSeconds = 60;

// Inline keys (dev)
private String privateKeyPem;
private String publicKeyPem;

// Or file locations (prod)
private String privateKeyLocation;
private String publicKeyLocation;

// Optional rotation block
public static class KeyDef {
 private String id;
 private String privateKeyLocation; // optional
 private String publicKeyLocation;
 // getters/setters
}
public static class Keyset {
 private String activeKeyId;
 private List<KeyDef> keys = List.of();
 // getters/setters
}
private Keyset keyset;

// getters/setters...
}
