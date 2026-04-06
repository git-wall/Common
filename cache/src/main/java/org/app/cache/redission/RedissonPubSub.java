package org.app.cache.redission;

import lombok.AllArgsConstructor;
import org.redisson.api.*;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed Pub/Sub via Redisson RTopic.
 * <p>
 * Unlike raw Redis pub/sub (Jedis/Lettuce), Redisson topics are <b>typed</b> —
 * messages are serialized/deserialized automatically via the configured codec.
 * No manual JSON parsing needed.
 *
 * <pre>{@code
 * RedissonPubSub pubsub = RedissonPubSub.of(factory);
 *
 * // Subscribe with typed message
 * pubsub.subscribe("user-events", UserEvent.class, (channel, event) -> {
 *     System.out.println("User event: " + event);
 * });
 *
 * // Publish typed object — no manual serialization
 * pubsub.publish("user-events", new UserEvent("alice", "LOGIN"));
 *
 * // Pattern subscribe (glob)
 * pubsub.psubscribe("events.*", UserEvent.class, (pattern, channel, event) -> {
 *     System.out.println("[" + channel + "] " + event);
 * });
 *
 * // Reliable topic — guaranteed delivery even if subscriber was offline
 * pubsub.subscribeReliable("critical-events", UserEvent.class, (channel, event) -> {
 *     process(event);
 * });
 *
 * pubsub.unsubscribe("user-events");
 * pubsub.close();
 * }</pre>
 */
public final class RedissonPubSub implements Closeable {

    private final RedissonClient client;

    /** Tracks listener IDs so we can unsubscribe cleanly. */
    private final Map<String, TopicRegistration> registrations = new ConcurrentHashMap<>();

    private RedissonPubSub(RedissonClient client) {
        this.client = client;
    }

    public static RedissonPubSub of(RedissonFactory factory) {
        return new RedissonPubSub(factory.client());
    }

    // =========================================================================
    // Standard Topic — typed, at-most-once delivery
    // =========================================================================

    /**
     * Subscribe to a typed topic.
     * The listener receives the full channel name and the deserialized message object.
     *
     * @param topicName  Redis key / channel name
     * @param messageType  expected message class (used for deserialization)
     * @param listener   (channelName, message) -> void
     */
    public <T> void subscribe(String topicName, Class<T> messageType,
                              java.util.function.BiConsumer<String, T> listener) {
        RTopic topic = client.getTopic(topicName);
        int listenerId = topic.addListener(messageType, (channel, msg) -> listener.accept(channel.toString(), msg));
        registrations.put(topicName, new TopicRegistration(topic, listenerId, false));
    }

    /**
     * Publish a typed message to a topic.
     * @return number of clients that received the message
     */
    public <T> long publish(String topicName, T message) {
        return client.getTopic(topicName).publish(message);
    }

    /** Async publish. */
    public <T> java.util.concurrent.CompletableFuture<Long> publishAsync(String topicName, T message) {
        return client.getTopic(topicName).publishAsync(message).toCompletableFuture();
    }

    /** Unsubscribe from a topic. Removes all listeners registered via this instance. */
    public void unsubscribe(String topicName) {
        TopicRegistration reg = registrations.remove(topicName);
        if (reg != null && !reg.reliable) {
            ((RTopic) reg.topic).removeListener(reg.listenerId);
        } else if (reg != null) {
            ((RReliableTopic) reg.topic).removeListener(String.valueOf(reg.listenerId));
        }
    }

    public long subscriberCount(String topicName) {
        return client.getTopic(topicName).countSubscribers();
    }

    // =========================================================================
    // Pattern Topic — subscribe by glob pattern
    // =========================================================================

    /**
     * Subscribe to all topics matching a glob pattern.
     *
     * <pre>
     * pubsub.psubscribe("events.*", UserEvent.class, (pattern, channel, event) -> { ... });
     * </pre>
     */
    public <T> void psubscribe(String pattern, Class<T> messageType,
                                TriConsumer<String, String, T> listener) {
        RPatternTopic topic = client.getPatternTopic(pattern);
        int id = topic.addListener(
            messageType, (ptn, channel, msg) -> listener.accept(ptn.toString(), channel.toString(), msg)
        );
        registrations.put("pattern:" + pattern, new TopicRegistration(topic, id, false));
    }

    public void punsubscribe(String pattern) {
        TopicRegistration reg = registrations.remove("pattern:" + pattern);
        if (reg != null) ((RPatternTopic) reg.topic).removeListener(reg.listenerId);
    }

    // =========================================================================
    // Reliable Topic — at-least-once, survives subscriber downtime
    // =========================================================================

    /**
     * Subscribe to a reliable topic.
     * <p>
     * Messages are stored in Redis until all subscribers acknowledge them.
     * If a subscriber is offline, it will receive missed messages on reconnect.
     * Ideal for critical event streams (audit logs, notifications, etc).
     *
     * @return subscriberId (needed to unsubscribe)
     */
    public <T> String subscribeReliable(String topicName, Class<T> messageType,
                                         java.util.function.BiConsumer<String, T> listener) {
        RReliableTopic topic = client.getReliableTopic(topicName);
        String subscriberId = topic.addListener(
            messageType, (channel, msg) -> listener.accept(channel.toString(), msg)
        );
        registrations.put("reliable:" + topicName, new TopicRegistration(topic, 0, true, subscriberId));
        return subscriberId;
    }

    public <T> long publishReliable(String topicName, T message) {
        return client.getReliableTopic(topicName).publish(message);
    }

    public void unsubscribeReliable(String topicName) {
        TopicRegistration reg = registrations.remove("reliable:" + topicName);
        if (reg != null) ((RReliableTopic) reg.topic).removeListener(reg.subscriberId);
    }

    // =========================================================================
    // Raw access
    // =========================================================================

    public RTopic rtopic(String name)           { return client.getTopic(name); }
    public RPatternTopic rpatternTopic(String p) { return client.getPatternTopic(p); }
    public RReliableTopic rreliableTopic(String n) { return client.getReliableTopic(n); }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Override
    public void close() {
        registrations.forEach((key, reg) -> {
            try {
                if (reg.reliable) {
                    ((RReliableTopic) reg.topic).removeListener(reg.subscriberId);
                } else if (reg.topic instanceof RTopic) {
                    var t = (RTopic) reg.topic;
                    t.removeAllListeners();
                } else if (reg.topic instanceof RPatternTopic) {
                    var pt = (RPatternTopic) reg.topic;
                    pt.removeAllListeners();
                }
            } catch (Exception ignored) {}
        });
        registrations.clear();
    }

    // =========================================================================
    // Internal
    // =========================================================================

    @AllArgsConstructor
     static class TopicRegistration {
        Object topic; int listenerId; boolean reliable; String subscriberId;
        TopicRegistration(Object topic, int listenerId, boolean reliable) {
            this(topic, listenerId, reliable, null);
        }
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
