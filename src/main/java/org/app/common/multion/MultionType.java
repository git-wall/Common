package org.app.common.multion;

import lombok.Getter;
import lombok.SneakyThrows;
import org.app.common.utils.ClassUtils;
import org.app.common.context.SpringContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Multion with Enum define type get class like factory
 *
 * @param <K> enum type, singleton
 * @param <V> class impl (service), singleton
 */
public class MultionType<K extends Enum<K>, V> {
    private final Map<K, V> multion = new HashMap<>(5, 0.7f);

    @Getter
    private final Class<K> enumClass;

    @Getter
    private final Class<V> interfaceClass;

    /**
     * Get a MultionType instance from the MultionManager
     *
     * @param enumClass the class of the enum type
     * @param interfaceClass the class of the interface type
     * @return a MultionType instance
     */
    public static <K extends Enum<K>, V> MultionType<K, V> of(Class<K> enumClass, Class<V> interfaceClass) {
        return MultionManager.getInstance().getMultionType(enumClass, interfaceClass);
    }

    /**
     * Constructs a MultionType instance by mapping enum constants to their corresponding
     * implementation classes. It attempts to instantiate each implementation class and
     * store it in a map with the enum constant as the key.
     *
     * @param enumClass the class of the enum type, which defines the keys for the map.
     * @param interfaceClass the class of the interface type, which defines the type of
     *                       the values in the map. The implementation classes of this
     *                       interface are used to create instances.
     */
    public MultionType(Class<K> enumClass, Class<V> interfaceClass) {
        this.enumClass = enumClass;
        this.interfaceClass = interfaceClass;

        K[] enums = enumClass.getEnumConstants();
        Set<Class<? extends V>> implClasses = ClassUtils.getClasses(interfaceClass);

        for (K enumConstant : enums) {
            String enumNameLower = enumConstant.name().toLowerCase();
            for (Class<?> implClass : implClasses) {
                String classNameLower = implClass.getSimpleName().toLowerCase();

                // Check if class name contains enum name
                if (classNameLower.contains(enumNameLower)) {
                    try {
                        V instance = getInstance(implClass);
                        multion.put(enumConstant, instance);
                        break; // Found a match, move to the next enum
                    } catch (Exception e) {
                        continue;
                        // Skip if instantiation fails
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private V getInstance(Class<?> implClass) throws Exception {
        ApplicationContext context;
        if ((context = SpringContext.getContext()) != null) {
            try {
                // Try to get bean from Spring context
                return (V) context.getBean(implClass);
            } catch (NoSuchBeanDefinitionException e) {
                // Fall back to regular instantiation if not a Spring bean
                return (V) implClass.getDeclaredConstructor().newInstance();
            }
        }
        // No Spring context available, use regular instantiation
        return (V) implClass.getDeclaredConstructor().newInstance();
    }

    public Map<K, V> getMultion() {
        return Collections.unmodifiableMap(multion);
    }

    public V get(K key) {
        return multion.get(key);
    }

    // be carefully this func
    // make sure you need to use this func if not, don't take
    // because which class implement interface always service and init in here
    @SneakyThrows
    @Deprecated
    @SuppressWarnings("unchecked")
    public V getNewValue(K key) {
        V v = multion.get(key);
        if (SpringContext.getContext() != null) return v;
        return (V) v.getClass().getDeclaredConstructor().newInstance();
    }
}
