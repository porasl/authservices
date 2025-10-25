package com.porasl.authservices.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String headerName;
    private final String expectedToken;

    public ApiKeyAuthFilter(
            @Value("${internal.header:X-Internal-Token}") String headerName,
            @Value("${internal.token}") String expectedToken) {
        this.headerName = headerName;
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String path = req.getRequestURI();
        log.debug("ApiKeyAuthFilter inspecting path: {}", path);

        // ✅ Skip authentication for public endpoints
        if (isPublicPath(path)) {
            log.trace("Skipping ApiKeyAuthFilter for public path: {}", path);
            chain.doFilter(req, res);
            return;
        }

        // ✅ Only protect /internal/* endpoints
        if (path != null && path.startsWith("/internal/")) {
            String token = req.getHeader(headerName);

            if (token == null) {
                log.warn("Missing {} header on /internal/ request", headerName);
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing internal authentication header");
                return;
            }

            if (!token.equals(expectedToken)) {
                log.warn("Invalid {} header: {}", headerName, token);
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal token");
                return;
            }

            // Mark request as authenticated with “SVC” authority
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "internal-client",
                            null,
                            List.of(new SimpleGrantedAuthority("SVC"))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Authenticated internal request from header {} for path {}", headerName, path);
        }

        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only filter /internal/*, skip everything else
        return path == null || !path.startsWith("/internal/");
    }

    private boolean isPublicPath(String path) {
        if (path == null) return false;
        return path.equals("/auth/register")
                || path.equals("/auth/login")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
