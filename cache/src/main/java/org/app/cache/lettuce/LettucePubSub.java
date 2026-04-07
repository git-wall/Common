package org.app.cache.lettuce;

import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Pub/Sub support via Lettuce.
 * <p>
 * Pub/Sub requires its own dedicated connection (separate from command connections).
 * This class manages the subscriber connection lifecycle.
 *
 * <pre>{@code
 * LettucePubSub pubsub = LettucePubSub.of(factory);
 *
 * // Subscribe to a channel
 * pubsub.subscribe("notifications", (channel, msg) -> {
 *     System.out.println("Got: " + msg + " on " + channel);
 * });
 *
 * // Subscribe to a pattern
 * pubsub.psubscribe("events.*", (pattern, channel, msg) -> {
 *     System.out.println("[" + pattern + "] " + channel + ": " + msg);
 * });
 *
 * // Publish (uses regular command connection)
 * pubsub.publish("notifications", "Hello subscribers");
 *
 * // Unsubscribe
 * pubsub.unsubscribe("notifications");
 *
 * pubsub.close(); // on shutdown
 * }</pre>
 */
public final class LettucePubSub implements Closeable {

    private final LettuceOps ops;
    private final StatefulRedisPubSubConnection<String, String> subConn;
    private final Map<String, BiConsumer<String, String>> channelHandlers  = new ConcurrentHashMap<>();
    private final Map<String, TriConsumer>                patternHandlers  = new ConcurrentHashMap<>();

    private LettucePubSub(LettuceClientFactory factory, LettuceOps ops) {
        this.ops     = ops;
        this.subConn = factory.connectPubSub();

        subConn.addListener(new RedisPubSubListener<>() {
            @Override public void message(String channel, String message) {
                BiConsumer<String, String> h = channelHandlers.get(channel);
                if (h != null) h.accept(channel, message);
            }
            @Override public void message(String pattern, String channel, String message) {
                TriConsumer h = patternHandlers.get(pattern);
                if (h != null) h.accept(pattern, channel, message);
            }
            @Override public void subscribed(String channel, long count)   {}
            @Override public void psubscribed(String pattern, long count)  {}
            @Override public void unsubscribed(String channel, long count) { channelHandlers.remove(channel); }
            @Override public void punsubscribed(String pattern, long count){ patternHandlers.remove(pattern); }
        });
    }

    public static LettucePubSub of(LettuceClientFactory factory) {
        return new LettucePubSub(factory, LettuceOps.of(factory));
    }

    // -------------------------------------------------------------------------
    // Subscribe
    // -------------------------------------------------------------------------

    /**
     * Subscribe to exact channel(s).
     * @param handler (channel, message) -> void
     */
    public void subscribe(String channel, BiConsumer<String, String> handler) {
        channelHandlers.put(channel, handler);
        subConn.sync().subscribe(channel);
    }

    /** Subscribe to multiple channels with same handler. */
    public void subscribe(BiConsumer<String, String> handler, String... channels) {
        for (String ch : channels) channelHandlers.put(ch, handler);
        subConn.sync().subscribe(channels);
    }

    /**
     * Subscribe to a pattern (glob-style).
     * @param handler (pattern, channel, message) -> void
     */
    public void psubscribe(String pattern, TriConsumer handler) {
        patternHandlers.put(pattern, handler);
        subConn.sync().psubscribe(pattern);
    }

    public void unsubscribe(String... channels) {
        subConn.sync().unsubscribe(channels);
    }

    public void punsubscribe(String... patterns) {
        subConn.sync().punsubscribe(patterns);
    }

    // -------------------------------------------------------------------------
    // Publish
    // -------------------------------------------------------------------------

    public long publish(String channel, String message) {
        return ops.publish(channel, message);
    }

    // -------------------------------------------------------------------------
    // Info
    // -------------------------------------------------------------------------

    public long subscriberCount(String channel) {
        return subConn.sync().pubsubNumsub(channel).getOrDefault(channel, 0L);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        subConn.close();
    }

    // -------------------------------------------------------------------------
    // Functional interface
    // -------------------------------------------------------------------------

    @FunctionalInterface
    public interface TriConsumer {
        void accept(String pattern, String channel, String message);
    }
}
