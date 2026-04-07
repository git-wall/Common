package org.app.security.shield.event;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.security.shield.constant.AuthFailureReason;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.security.SignatureException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthFailureReasonMapper {

    public static AuthFailureReason map(Exception ex) {

        if (ex instanceof AuthenticationCredentialsNotFoundException) {
            return AuthFailureReason.NO_TOKEN;
        }

        if (ex instanceof ExpiredJwtException) {
            return AuthFailureReason.TOKEN_EXPIRED;
        }

        if (ex instanceof SignatureException) {
            return AuthFailureReason.INVALID_SIGNATURE;
        }

        if (ex instanceof MalformedJwtException) {
            return AuthFailureReason.MALFORMED_TOKEN;
        }

        if (ex instanceof InsufficientAuthenticationException) {
            return AuthFailureReason.INSUFFICIENT_SCOPE;
        }

        return AuthFailureReason.UNKNOWN;
    }
}
