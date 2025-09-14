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

//com.porasl.authservices.security.ApiKeyAuthFilter
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
@Value("${internal.token}") private String internalToken;

@Override protected boolean shouldNotFilter(HttpServletRequest req) {
 return !req.getRequestURI().startsWith("/internal/");
}

@Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
   throws IOException, ServletException {
 String header = req.getHeader("X-Internal-Token");
 if (internalToken != null && !internalToken.isBlank() && internalToken.equals(header)) {
   var auth = new UsernamePasswordAuthenticationToken(
       "internal-client", null, List.of(new SimpleGrantedAuthority("SVC")));
   SecurityContextHolder.getContext().setAuthentication(auth);
   chain.doFilter(req, res);
 } else {
   res.setStatus(HttpServletResponse.SC_FORBIDDEN);
 }
}
}
