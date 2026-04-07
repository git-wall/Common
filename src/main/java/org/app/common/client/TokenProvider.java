package org.app.common.client;

import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class TokenProvider {
    private String cachedToken;
    private final ReentrantLock lock = new ReentrantLock();
    private final Supplier<OAuth2AccessToken> tokenFetcher;

    public TokenProvider(Supplier<OAuth2AccessToken> tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
    }

    public String getToken() {
        if (cachedToken != null) {
            return cachedToken;
        }

        lock.lock();
        try {
            if (cachedToken != null) {
                return cachedToken;
            }
            cachedToken = tokenFetcher.get().getTokenValue();
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }

    public String refreshToken() {
        lock.lock();
        try {
            cachedToken = tokenFetcher.get().getTokenValue();
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }
}
