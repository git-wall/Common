package org.app.exception;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class to locate caller stack frames, returning a compact textual
 * representation of stack locations.
 *
 * <p>
 * This class uses {@link java.lang.StackWalker} (Java 9+) with the
 * {@link java.lang.StackWalker.Option#RETAIN_CLASS_REFERENCE} option so that
 * class names can be examined reliably. It exposes a single convenient method
 * {@link #findLocation(int)} which returns a " -> "-separated list of
 * "class.method:line" entries for the first non-ignored frames in the current
 * thread's stack.
 * </p>
 *
 * <p>
 * There are two ways to control which frames are ignored:
 * <ul>
 *   <li>By default, any class whose fully-qualified name starts with an entry
 *       in {@link #IGNORE_PACKAGES} (e.g. "java.", "sun.", "jdk.") will be
 *       ignored.</li>
 *   <li>Call {@link #registerIgnoreClass(Class)} to add additional specific
 *       classes (by Class object) to the ignore set at runtime.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Thread-safety: this class is safe for concurrent use. The ignore set uses a
 * concurrent backing set obtained from {@link ConcurrentHashMap#newKeySet()}.
 * </p>
 *
 * Usage examples:
 * <pre>
 *   // simple: get the first non-ignored location
 *   String loc = StackLocators.findLocation(1);
 *
 *   // ignore a helper class so it doesn't appear in results
 *   StackLocators.registerIgnoreClass(MyHelper.class);
 *   String twoFrames = StackLocators.findLocation(2);
 * </pre>
 *
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StackLocators {

    /**
     * A {@link StackWalker} instance configured to retain class references.
     *
     * <p>
     * RETAIN_CLASS_REFERENCE allows callers to call {@link
     * java.lang.StackWalker.StackFrame#getClassName} and other class-related
     * operations reliably. Using a single shared {@code StackWalker} is
     * efficient and the StackWalker instances are lightweight and intended to
     * be reused.
     * </p>
     */
    private static final StackWalker WALKER =
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Concurrent set of fully-qualified class names to ignore when scanning the
     * stack. Backed by {@link ConcurrentHashMap#newKeySet()} which provides
     * thread-safe add/contains semantics.
     *
     * <p>
     * Entries are class names (String) obtained from {@link Class#getName()}.
     * The set is populated with {@link StackLocators} itself in the static
     * initializer so that calls originating inside this utility do not appear
     * in results.
     * </p>
     */
    private static final Set<String> IGNORE_CLASSES = ConcurrentHashMap.newKeySet();

    private static final List<String> IGNORE_PACKAGES = List.of(
        "java.",
        "sun.",
        "jdk."
    );

    private static final Function<StackWalker.StackFrame, String> FRAME_TO_STRING = f ->
        String.format("%s.%s:%s", f.getClassName(), f.getMethodName(), f.getLineNumber());

    static {
        // Ensure this utility class itself is ignored by default so callers see
        // the application frame that invoked the APIs, not the frames inside
        // this helper.
        IGNORE_CLASSES.add(StackLocators.class.getName());
    }

    /**
     * Add a class to the ignore set so frames originating from it will be
     * skipped by {@link #findLocation(int)}.
     *
     * <p>
     * Example: call {@code StackLocators.registerIgnoreClass(MyHelper.class)}
     * if MyHelper should not appear in the computed stack locations.
     * </p>
     *
     * @param clazz the class to ignore (must not be {@code null})
     */
    public static void registerIgnoreClass(Class<?> clazz) {
        IGNORE_CLASSES.add(clazz.getName());
    }

    private static boolean isIgnored(String className) {
        if (IGNORE_CLASSES.contains(className)) return true;

        for (String p : IGNORE_PACKAGES) {
            if (className.startsWith(p)) return true;
        }
        return false;
    }

    /**
     * Find a textual representation of the first non-ignored stack frames up to
     * the supplied limit.
     *
     * <p>
     * This method walks the current thread's stack and:
     * <ol>
     *   <li>drops leading frames while {@link #isIgnored(String)} returns true</li>
     *   <li>takes up to {@code limit} frames after the dropped frames</li>
     *   <li>maps each frame to {@link #FRAME_TO_STRING}</li>
     *   <li>joins them with " -> " into a single {@link String}</li>
     * </ol>
     * If no non-ignored frame is found the method returns the string {@code "unknown"}.
     * </p>
     *
     * @param limit maximum number of frames to include in the result (must be >= 0)
     * @return a single-line string containing up to {@code limit} locations joined
     *         with " -> ", or {@code "unknown"} if none were found
     */
    public static String findLocation(int limit) {
        return WALKER.walk(getStackAsString(limit)).orElse("unknown");
    }

    private static Function<Stream<StackWalker.StackFrame>, Optional<String>> getStackAsString(int limit) {
        return s ->
            s.dropWhile(f -> isIgnored(f.getClassName()))
                .limit(limit)
                .map(FRAME_TO_STRING)
                .reduce((a, b) -> a + " -> " + b);
    }
}
