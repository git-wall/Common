package org.app.common.usecase;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @param <R> the type of the result produced by the chain fetcher <br>
 * <pre>
 * {@code
 * AtomicReference<String> cache = new AtomicReference<>();
 *
 * Consumer<String> cacheUpdater = cache::set;
 *
 * Supplier<String> fastSource = () -> null;
 * Supplier<String> remoteSource = () -> "remote-value";
 * Supplier<String> flakySource = () -> { throw new RuntimeException("fail"); };
 *
 * ChainFetcher<String> fetcher = new ChainFetcher<>(cacheUpdater, fastSource, remoteSource, flakySource);
 *
 * String v = fetcher.get();
 * String tryV = fetcher.tryGet(flakySource);
 * }
 * </pre>
 */
@Slf4j
public class ChainFetcher<R> {
    private final Consumer<R> cacheUpdater;
    private final Supplier<R>[] sources;

    @SafeVarargs
    public ChainFetcher(Consumer<R> cacheUpdater, Supplier<R>... sources) {
        this.sources = sources;
        this.cacheUpdater = cacheUpdater;
    }

    public R get() {
        for (Supplier<R> s : sources) {
            R v = s.get();
            if (v != null) {
                cacheUpdater.accept(v);
                return v;
            }
        }
        return null;
    }

    public R tryGet(Supplier<R> source) {
        try {
            R v = source.get();
            if (v != null) {
                cacheUpdater.accept(v);
            }
            return v;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
