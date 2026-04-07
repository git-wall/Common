package org.app.common.module.intent.engine;

import lombok.RequiredArgsConstructor;
import org.app.common.module.intent.core.Intent;
import org.app.common.module.intent.core.IntentStatus;
import org.app.common.module.intent.idempotency.IntentStore;
import org.app.common.module.intent.spi.IntentStrategy;

import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
public class IntentProcessor {

    private final IntentStore store;
    private final Map<String, IntentStrategy<?>> strategies;
    private final Duration ttl;

    @SuppressWarnings("unchecked")
    public <T> Intent<T> process(
        String intentId,
        String type,
        T payload) {

        Intent<T> intent = Intent.<T>builder()
            .intentId(intentId)
            .type(type)
            .payload(payload)
            .status(IntentStatus.PROCESSING)
            .createdAt(System.currentTimeMillis())
            .build();

        intent = store.createOrGet(intent, ttl);

        if (intent.getStatus() != IntentStatus.PROCESSING) {
            return intent;
        }

        IntentStrategy<T> strategy = (IntentStrategy<T>) strategies.get(type);

        strategy.executeAsync(intent);
        return intent;
    }
}

