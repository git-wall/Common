package org.app.common.eav;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class DynamicClass<T> {
    private final T originalInstance;
    private Collection<EAV> EAVs;

    public DynamicClass(T originalInstance) {
        this.originalInstance = originalInstance;
    }

    public DynamicClass<T> fields(Collection<EAV> EAVs) {
        this.EAVs = EAVs;
        return this;
    }

    private Field[] getFields() {
        return originalInstance.getClass().getDeclaredFields();
    }

    public T build() {
        try {
            DynamicType.Builder<?> builder = createBuilder();
            Class<?> dynamicClass = loadClass(builder);
            Object instance = dynamicClass.getDeclaredConstructor().newInstance();
            copyFields(dynamicClass, instance);
            addNewFields(dynamicClass, instance);
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build dynamic class", e);
        }
    }
    private DynamicType.Builder<?> createBuilder() throws IllegalAccessException {
        DynamicType.Builder<?> builder = defineClass();
        builder = defineField(builder);
        builder = defineNewField(builder);
        return builder;
    }

    private DynamicType.Builder<?> defineClass() {
        return new ByteBuddy()
                .subclass(originalInstance.getClass())
                .annotateType(
                        AnnotationDescription.Builder
                                .ofType(JsonAutoDetect.class)
                                .define("fieldVisibility", JsonAutoDetect.Visibility.ANY)
                                .build()
                );
    }

    private DynamicType.Builder<?> defineNewField(DynamicType.Builder<?> builder) {
        for (EAV attr : EAVs) {
            builder = builder.defineField(
                    attr.getName(),
                    attr.getType(),
                    Modifier.PRIVATE
            );
        }
        return builder;
    }

    private DynamicType.Builder<?> defineField(DynamicType.Builder<?> builder) throws IllegalAccessException {
        for (Field field : getFields()) {
            field.setAccessible(true);

            Object fieldValue = field.get(originalInstance);
            String getterName = String.format("get%s", capitalize(field.getName()));

            // Override the getter method to return the value from the original instance
            builder = builder
                    .method(ElementMatchers.named(getterName))
                    .intercept(FixedValue.value(fieldValue));

        }
        return builder;
    }

    private Class<?> loadClass(DynamicType.Builder<?> builder) {
        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            return unloaded.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
        }
    }

    private void copyFields(Class<?> dynamicClass, Object instance) throws IllegalAccessException, NoSuchFieldException {
        for (Field originalField : getFields()) {
            originalField.setAccessible(true);
            Object value = originalField.get(originalInstance);

            Field newField = dynamicClass.getDeclaredField(originalField.getName());
            newField.setAccessible(true);
            newField.set(instance, value);
        }
    }

    private void addNewFields(Class<?> dynamicClass, Object instance) throws NoSuchFieldException, IllegalAccessException {
        for (EAV attr : EAVs) {
            Field field = dynamicClass.getDeclaredField(attr.getName());
            field.setAccessible(true);
            field.set(instance, attr.getValue());
        }
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
