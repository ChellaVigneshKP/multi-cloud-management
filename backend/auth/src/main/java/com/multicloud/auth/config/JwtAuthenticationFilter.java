package com.multicloud.auth.config;

import com.multicloud.auth.service.JweService;
import com.multicloud.commonlib.exceptions.JweDecryptionException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JweService jweService;
    private final UserDetailsService userDetailsService;
    private static final String JWT_COOKIE_NAME = "jweToken";
    private static final Logger jwtLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/login", "/auth/signup", "/auth/verify",
            "/auth/resend", "/auth/forgot-password", "/auth/reset-password",
            "/auth/refresh-token", "/auth/logout", "/auth/take-action",
            "/auth/health", "/auth/actuator/health", "/auth/actuator/prometheus",
            "/auth/v3/**", "/auth/swagger-ui/**", "/actuator/**"
    );

    public JwtAuthenticationFilter(JweService jweService, UserDetailsService userDetailsService) {
        this.jweService = jweService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean isExcluded = EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        jwtLogger.debug("Path: {}, Should Not Filter: {}", path, isExcluded);
        return isExcluded;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);

        if (jwtCookie == null) {
            jwtLogger.warn("No JWT token found in cookie, denying access");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing authentication token");
            return;
        }

        String token = jwtCookie.getValue();
        try {
            String username = jweService.extractEmail(token);
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && existingAuth == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jweService.isTokenValid(token, userDetails)) {
                    setAuthentication(request, userDetails);
                } else {
                    denyAccess(response, "Unauthorized: Invalid token");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (JweDecryptionException e) {
            jwtLogger.error("Error processing JWT token");
            denyAccess(response, "Unauthorized: Invalid token format");
        } catch (Exception e) {
            jwtLogger.error("Error processing JWT token", e);
            denyAccess(response, "Unauthorized: Authentication error");
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        jwtLogger.info("User authenticated: {}", userDetails.getUsername());
    }

    private void denyAccess(HttpServletResponse response,String message) throws IOException {
        jwtLogger.warn("Invalid authentication token");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(message);
    }
}
