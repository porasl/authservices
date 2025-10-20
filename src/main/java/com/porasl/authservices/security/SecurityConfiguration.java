package com.porasl.authservices.security;

import static com.porasl.authservices.user.Permission.*;
import static com.porasl.authservices.user.Role.ADMIN;
import static com.porasl.authservices.user.Role.MANAGER;
import static org.springframework.http.HttpMethod.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

  @Autowired private JwtAuthenticationFilter jwtAuthFilter;
  @Autowired private AuthenticationProvider authenticationProvider;
  @Autowired private ApiKeyAuthFilter apiKeyAuthFilter;
  @Autowired private org.springframework.security.web.authentication.logout.LogoutHandler logoutHandler;

  @Bean(name = "authServicesSecurityFilterChain")
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
      )
      .authorizeHttpRequests(req -> req
        // allow preflight (harmless for Postman, required for browsers)
        .requestMatchers(OPTIONS, "/**").permitAll()

        // 👇 only the auth endpoints are public
        .requestMatchers(POST, "/auth/authenticate").permitAll()
        .requestMatchers(POST, "/auth/authenticateWithToken").permitAll()

        // Swagger & static docs (keep if you use them)
        .requestMatchers(
        		"/auth/authenticate",
            "/auth/authenticateWithToken",
            "/api-docs", 
            "/api-docs/**",
            "/swagger-resources", 
            "/swagger-resources/**",
            "/configuration/ui", 
            "/configuration/security",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/auth/swagger-ui.html",
            "/auth/index.html",
            "/webjars/**"
        ).permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
        // public profile image fetches (HEAD/GET only)
        .requestMatchers(HEAD, "/api/profile/image/**").permitAll()
        .requestMatchers(GET,  "/api/profile/image/**").permitAll()

        // Internal service endpoints (API key filter will run here)
        .requestMatchers("/internal/users/**").hasAuthority("SVC")

        // Management RBAC
        .requestMatchers("/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
        .requestMatchers(GET,    "/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
        .requestMatchers(POST,   "/management/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
        .requestMatchers(PUT,    "/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
        .requestMatchers(DELETE, "/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())

        // 🚫 IMPORTANT: remove any broad "/auth/**.permitAll()" — it would expose /auth/api/**
        // Everything else must be authenticated (this includes /auth/api/me/connections/**)
        .anyRequest().authenticated()
      )
      .authenticationProvider(authenticationProvider)
      // API key filter first (it will SKIP non-/internal/** via shouldNotFilter)
      .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
      // then your JWT filter
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .logout(logout -> logout
        .logoutUrl("/auth/logout")
        .addLogoutHandler(logoutHandler)
        .logoutSuccessHandler((req, res, auth) -> SecurityContextHolder.clearContext())
      );

    return http.build();
  }
}
