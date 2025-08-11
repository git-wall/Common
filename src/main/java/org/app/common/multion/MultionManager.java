package org.app.common.multion;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for MultionType instances.
 * Provides centralized access and tracking of MultionType instances.
 */
@Slf4j
public class MultionManager {

    private static final MultionManager INSTANCE = new MultionManager();

    // Store MultionType instances by their type signature
    private final Map<Class<?>, Object> multionInstances = new ConcurrentHashMap<>(16, 0.75f, 16);

    private MultionManager() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Get the singleton instance of MultionManager
     *
     * @return The MultionManager instance
     */
    public static MultionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Get or create a MultionType instance for the given enum and interface classes
     *
     * @param enumClass      The enum class that defines the keys
     * @param interfaceClass The interface class that defines the values
     * @param <K>            The enum type
     * @param <V>            The interface type
     * @return A MultionType instance
     */
    public <K extends Enum<K>, V> MultionType<K, V> getMultionType(Class<K> enumClass, Class<V> interfaceClass) {
        @SuppressWarnings("unchecked")
        var multionType = (MultionType<K, V>) multionInstances.computeIfAbsent(
                enumClass,
                k -> new MultionType<>(enumClass, interfaceClass)
        );

        return multionType;
    }

    /**
     * Register a custom MultionType instance
     *
     * @param enumClass      The enum class that defines the keys
     * @param multionType    The MultionType instance to register
     * @param <K>            The enum type
     * @param <V>            The interface type
     */
    public <K extends Enum<K>, V> void registerMultionType(Class<K> enumClass, MultionType<K, V> multionType) {
        multionInstances.put(enumClass, multionType);
    }

    /**
     * Register a custom MultionType instance
     *
     * @param enumClass      The enum class that defines the keys
     * @param interfaceClass The interface class that defines the values
     */
    public void registerMultionType(Class<?> enumClass, Class<?> interfaceClass) {
        multionInstances.put(enumClass, new MultionType(enumClass, interfaceClass));
    }

    /**
     * Register a custom MultionType instance
     *
     * @param multionTypes      The map has class and interface defines for many multion type
     */
    public void registerMultionTypes(Map<Class<?>, Class<?>> multionTypes) {
        multionTypes.forEach((key,value) -> multionInstances.put(key, new MultionType(key, value)));
    }

    /**
     * Check if a MultionType instance exists for the given enum and interface classes
     *
     * @param enumClass The enum class that defines the keys
     * @param <K>       The enum type
     * @return True if a MultionType instance exists, false otherwise
     */
    public <K extends Enum<K>> boolean hasMultionType(Class<K> enumClass) {
        return multionInstances.containsKey(enumClass);
    }

    /**
     * Remove a MultionType instance
     *
     * @param enumClass      The enum class that defines the keys
     * @param interfaceClass The interface class that defines the values
     * @param <K>            The enum type
     * @param <V>            The interface type
     * @return They removed MultionType instance, or null if none existed
     */
    @SuppressWarnings("unchecked")
    public <K extends Enum<K>, V> MultionType<K, V> removeMultionType(Class<K> enumClass, Class<V> interfaceClass) {
        MultionType<K, V> removed = (MultionType<K, V>) multionInstances.remove(enumClass);
        if (removed != null) {
            log.info("Removed MultionType for enum: {} and interface: {}",
                    enumClass.getSimpleName(), interfaceClass.getSimpleName());
        }
        return removed;
    }

    /**
     * Get the number of MultionType instances being managed
     *
     * @return The number of MultionType instances
     */
    public int getMultionTypeCount() {
        return multionInstances.size();
    }

    /**
     * Clear all MultionType instances
     */
    public void clearAll() {
        multionInstances.clear();
        log.info("Cleared all MultionType instances");
    }
}