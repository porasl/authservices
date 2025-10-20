package com.porasl.authservices.security;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(0)
public class AuthHeaderLogger extends OncePerRequestFilter {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthHeaderLogger.class);
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    if (req.getRequestURI().startsWith("/auth/api/me/")) {
      String h = req.getHeader("Authorization");
      log.info("Authorization header: '{}'", h); // should be: Bearer eyJ...
    }
    chain.doFilter(req, res);
  }
}