
// PemKeyLoader.java
package com.porasl.authservices.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import java.util.Base64;

public final class PemKeyLoader {
  private PemKeyLoader() {}

  public static PrivateKey readPrivateKey(String pem) {
    String content = pem.replaceAll("-----\\w+ PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(content);
    try {
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
      return KeyFactory.getInstance("RSA").generatePrivate(spec);
    } catch (Exception e) { throw new RuntimeException("Invalid private key", e); }
  }

  public static PublicKey readPublicKey(String pem) {
    String content = pem.replaceAll("-----\\w+ PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(content);
    try {
      X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
      return KeyFactory.getInstance("RSA").generatePublic(spec);
    } catch (Exception e) { throw new RuntimeException("Invalid public key", e); }
  }
}
