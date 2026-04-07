package org.app.cache.cache2lv;

import java.time.Instant;

/**
 * Internal value holder stored in L1 local cache.
 * Tracks expiry, refresh state, and stale status.
 */
final class CacheEntry<V> {

    enum State { FRESH, STALE, REFRESHING }

    final V       value;
    final Instant expiresAt;          // hard expiry — evict after this
    final Instant refreshAfter;       // soft expiry — trigger background refresh
    volatile State state;

    CacheEntry(V value, Instant expiresAt, Instant refreshAfter) {
        this.value        = value;
        this.expiresAt    = expiresAt;
        this.refreshAfter = refreshAfter;
        this.state        = State.FRESH;
    }

    boolean isHardExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    boolean needsRefresh() {
        return state == State.FRESH && Instant.now().isAfter(refreshAfter);
    }

    /** Optimistic CAS: mark as REFRESHING so only one thread triggers refresh. */
    boolean markRefreshing() {
        if (state == State.FRESH || state == State.STALE) {
            state = State.REFRESHING;
            return true;
        }
        return false;
    }
}
