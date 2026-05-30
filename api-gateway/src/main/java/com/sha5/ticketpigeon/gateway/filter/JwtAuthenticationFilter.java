package com.sha5.ticketpigeon.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth"
    );

    // Paths that are public for GET but secured for POST/PUT/DELETE
    private static final List<String> READ_PUBLIC_PATHS = List.of(
            "/api/v1/movies",
            "/api/v1/theaters"
    );

    // Paths that require ADMIN role for POST/PUT/DELETE
    private static final List<String> ADMIN_WRITE_PATHS = List.of(
            "/api/v1/movies"
    );

    // Paths that require ADMIN or THEATER_ADMIN role for POST/PUT/DELETE
    private static final List<String> THEATER_ADMIN_WRITE_PATHS = List.of(
            "/api/v1/theaters"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Fully public paths (auth endpoints) — allow, but still stamp the secret
        if (isPublicPath(path)) {
            return forwardWithSecret(exchange, chain);
        }

        // 2. GET requests on read-public paths — allow without token
        if (method == HttpMethod.GET && isReadPublicPath(path)) {
            return forwardWithSecret(exchange, chain);
        }

        // 3. Actuator health endpoint — allow
        if (path.startsWith("/actuator")) {
            return forwardWithSecret(exchange, chain);
        }

        // 4. For everything else, a valid JWT is required
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = extractClaims(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Check token expiration
        if (claims.getExpiration().before(new Date())) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String role = claims.get("role", String.class);
        String email = claims.getSubject();

        // 5. Role-Based Access Control for write operations
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE) {
            // POST /api/v1/movies/** requires ADMIN role
            if (isAdminWritePath(path) && !"ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            // POST /api/v1/theaters/** requires ADMIN or THEATER_ADMIN role
            if (isTheaterAdminWritePath(path) && !"ADMIN".equals(role) && !"THEATER_ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // 6. Forward user info + gateway secret as headers to downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .header("X-Gateway-Secret", secret)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Forwards the request to downstream services with the gateway secret header.
     * Used for public paths that don't need JWT but still need the secret stamp
     * so downstream services know the request came through the gateway.
     */
    private Mono<Void> forwardWithSecret(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest stamped = exchange.getRequest().mutate()
                .header("X-Gateway-Secret", secret)
                .build();
        return chain.filter(exchange.mutate().request(stamped).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isReadPublicPath(String path) {
        return READ_PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminWritePath(String path) {
        return ADMIN_WRITE_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isTheaterAdminWritePath(String path) {
        return THEATER_ADMIN_WRITE_PATHS.stream().anyMatch(path::startsWith);
    }

    private Claims extractClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
