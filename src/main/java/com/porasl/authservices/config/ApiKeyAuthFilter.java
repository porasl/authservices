
package com.porasl.authservices.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private final String headerName;
  private final String expectedToken;

  // We inject BOTH the header name (with a sensible default) and the token
  public ApiKeyAuthFilter(
      @Value("${internal.header:X-Internal-Token}") String headerName,
      @Value("${internal.token}") String expectedToken) {
    this.headerName = headerName;
    this.expectedToken = expectedToken;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    if (req.getRequestURI() != null && req.getRequestURI().startsWith("/internal/")) {
      String token = req.getHeader(headerName);
      if (token == null || !token.equals(expectedToken)) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      // Mark request as authenticated with the SAME authority your config expects:
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              "internal-client",
              null,
              List.of(new SimpleGrantedAuthority("SVC")) // <— matches hasAuthority("SVC")
          );
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    chain.doFilter(req, res);
  }
}
