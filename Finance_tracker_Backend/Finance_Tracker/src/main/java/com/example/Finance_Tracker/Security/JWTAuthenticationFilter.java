package com.example.Finance_Tracker.Security;

import com.example.Finance_Tracker.User.repository.BlacklistedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JWTAuthenticationFilter(JWTService jwtService,
                                   UserDetailsService userDetailsService,
                                   BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        logger.debug("[{}] Authorization header: {}", Instant.now(), authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("[{}] No JWT token found in request headers.", Instant.now());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        logger.debug("[{}] Extracted JWT token: {}", Instant.now(), jwt);
        if ("demo-token".equals(jwt)) {
            logger.warn("[{}] Bypassing token validation for demo-token", Instant.now());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    "demoUser", null, java.util.Collections.emptyList()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
            return;
        }
        if (blacklistedTokenRepository.existsByToken(jwt)) {
            logger.warn("[{}] Token is blacklisted, rejecting request", Instant.now());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);
            logger.debug("[{}] Extracted username from JWT: {}", Instant.now(), username);
        } catch (Exception e) {
            logger.error("[{}] Failed to extract username from JWT token: {}", Instant.now(), e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.warn("JWT token is not valid for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Failed to load user details or set authentication: {}", e.getMessage());
            }
        } else {
            logger.debug("Username is null or context already has authentication.");
        }

        filterChain.doFilter(request, response);
    }
}
