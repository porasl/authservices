package com.porasl.authservices.config;

import static com.porasl.authservices.user.Permission.ADMIN_CREATE;
import static com.porasl.authservices.user.Permission.ADMIN_DELETE;
import static com.porasl.authservices.user.Permission.ADMIN_READ;
import static com.porasl.authservices.user.Permission.ADMIN_UPDATE;
import static com.porasl.authservices.user.Permission.MANAGER_CREATE;
import static com.porasl.authservices.user.Permission.MANAGER_DELETE;
import static com.porasl.authservices.user.Permission.MANAGER_READ;
import static com.porasl.authservices.user.Permission.MANAGER_UPDATE;
import static com.porasl.authservices.user.Role.ADMIN;
import static com.porasl.authservices.user.Role.MANAGER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String[] WHITE_LIST_URL = {
    		"/auth/**",
            "/api-docs",
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
            "/webjars/**"};
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Autowired
    private AuthenticationProvider authenticationProvider;
    
    @Autowired
    private LogoutHandler logoutHandler;

    @Bean(name = "authServicesSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http.csrf(AbstractHttpConfigurer::disable)
        // Ensure HTTP access is allowed (remove HTTPS enforcement if present)
        .authorizeHttpRequests(req ->
            req.requestMatchers("/auth/register").permitAll() // Allow unauthenticated access
               .requestMatchers("/auth/**").permitAll() // Allow access for other `/auth/**` endpoints
               .requestMatchers("/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
               .requestMatchers(GET, "/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
               .requestMatchers(POST, "/management/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
               .requestMatchers(PUT, "/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
               .requestMatchers(DELETE, "/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())
               .anyRequest().authenticated() // All other endpoints require authentication
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session management
        .authenticationProvider(authenticationProvider) // Authentication Provider configuration
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout ->
                    logout.logoutUrl("/auth/logout")
                            .addLogoutHandler(logoutHandler)
                            .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()) // Clear SecurityContext post-logout
            );

        return http.build();
    }
}