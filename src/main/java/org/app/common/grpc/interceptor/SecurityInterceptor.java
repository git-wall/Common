package org.app.common.grpc.interceptor;

import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.grpc.GrpcProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Interceptor for handling authentication and authorization in gRPC calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityInterceptor implements ServerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final GrpcProperties grpcProperties;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        if (!grpcProperties.getAuth().isEnabled()) {
            return next.startCall(call, headers);
        }

        // Extract the auth token from headers
        String token = extractToken(headers);

        if (token == null) {
            log.warn("Missing authentication token for method: {}", call.getMethodDescriptor().getFullMethodName());
            call.close(Status.UNAUTHENTICATED.withDescription("Authentication token is missing"), new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }

        try {
            // Validate the token
            if (!validateToken(token)) {
                log.warn("Invalid authentication token for method: {}", call.getMethodDescriptor().getFullMethodName());
                call.close(Status.PERMISSION_DENIED.withDescription("Permission denied"), new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            // If authentication passes, proceed with the call
            return next.startCall(call, headers);
        } catch (Exception e) {
            log.error("Error during authentication", e);
            call.close(Status.INTERNAL
                            .withDescription("Internal authentication error"),
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }
    }

    private String extractToken(Metadata headers) {
        String value = headers.get(Metadata.Key.of(AUTH_HEADER, Metadata.ASCII_STRING_MARSHALLER));
        if (value == null || !value.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return value.substring(BEARER_PREFIX.length());
    }

    private boolean validateToken(String token) {
        String secret = grpcProperties.getAuth().getTokenSecret();
        if (secret == null || secret.isEmpty()) {
            log.error("Token secret is not configured");
            return false;
        }

        try {
            // Ensure the secret is at least 32 bytes for HS256
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            SecretKey key = Keys.hmacShaKeyFor(secretBytes);

            // Parse and validate the JWT token
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check if token is expired
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.warn("Token is expired");
                return false;
            }

            // Check issuer if configured
            String issuer = grpcProperties.getAuth().getIssuer();
            if (issuer != null && !issuer.isEmpty()) {
                String tokenIssuer = claims.getIssuer();
                if (tokenIssuer == null || !tokenIssuer.equals(issuer)) {
                    log.warn("Invalid token issuer: {}", tokenIssuer);
                    return false;
                }
            }

            // Check audience if configured
            String audience = grpcProperties.getAuth().getAudience();
            if (audience != null && !audience.isEmpty()) {
                // Handle both string and collection audience types
                Object tokenAudience = claims.get("aud");
                if (tokenAudience == null) {
                    log.warn("Missing audience in token");
                    return false;
                }

                // Check if audience matches
                boolean audienceMatches = false;
                if (tokenAudience instanceof String) {
                    audienceMatches = audience.equals(tokenAudience);
                } else if (tokenAudience instanceof Iterable<?>) {
                    for (Object aud : (Iterable<?>) tokenAudience) {
                        if (audience.equals(aud.toString())) {
                            audienceMatches = true;
                            break;
                        }
                    }
                }

                if (!audienceMatches) {
                    log.warn("Invalid token audience: {}", tokenAudience);
                    return false;
                }
            }

            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }
}