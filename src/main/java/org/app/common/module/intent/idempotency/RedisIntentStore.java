package org.app.common.module.intent.idempotency;

import com.fasterxml.jackson.databind.JavaType;
import lombok.RequiredArgsConstructor;
import org.app.common.module.intent.core.Intent;
import org.app.common.module.intent.core.IntentStatus;
import org.app.common.utils.JacksonUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@RequiredArgsConstructor
public class RedisIntentStore implements IntentStore {

    private final RedisTemplate<String, Object> redis;
    private static final JavaType Intent = JacksonUtils.typeOf(Intent.class);

    private String key(String id) {
        return "intent:" + id;
    }

    @Override
    public <T> Intent<T> createOrGet(Intent<T> intent, Duration ttl) {
        Boolean created = redis.opsForValue().setIfAbsent(key(intent.getIntentId()), intent, ttl);
        return Boolean.TRUE.equals(created)
            ? intent
            : JacksonUtils.convert(redis.opsForValue().get(key(intent.getIntentId())), Intent);
    }

    @Override
    public <T> Intent<T> get(String intentId) {
        return JacksonUtils.convert(redis.opsForValue().get(key(intentId)), Intent);
    }

    @Override
    public void updateStatus(String intentId, IntentStatus status) {
        Intent<?> intent = get(intentId);
        if (intent == null) return;
        intent.setStatus(status);
        redis.opsForValue().set(key(intentId), intent);
    }

    @Override
    public boolean isExpired(Intent<?> intent) {
        return System.currentTimeMillis() - intent.getCreatedAt() > 10 * 60_000;
    }
}
