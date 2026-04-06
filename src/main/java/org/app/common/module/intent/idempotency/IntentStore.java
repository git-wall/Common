package org.app.common.module.intent.idempotency;

import org.app.common.module.intent.core.Intent;
import org.app.common.module.intent.core.IntentStatus;

import java.time.Duration;

public interface IntentStore {

    <T> Intent<T> createOrGet(Intent<T> intent, Duration ttl);

    <T> Intent<T> get(String intentId);

    void updateStatus(String intentId, IntentStatus status);

    boolean isExpired(Intent<?> intent);
}
