package org.app.common.eav.v2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DynamicClassBuilder<T> {

    private final Class<T> originalClass;
    private final String key;
    private final Iterable<EAVField> EAVs;

    public DynamicClassBuilder(Class<T> originalClass, String id, Iterable<EAVField> EAVs) {
        this.originalClass = originalClass;
        this.key = originalClass.getSimpleName() + "_" + id;
        this.EAVs = EAVs;
    }

    public DynamicClassBuilder(Class<T> originalClass, String key, EAVField... EAVs) {
        this(originalClass, key, Arrays.asList(EAVs));
    }

    public static <T> DynamicClassBuilder<T> of(Class<T> originalClass, String id, Iterable<EAVField> EAVs) {
        return new DynamicClassBuilder<>(originalClass, id, EAVs);
    }

    public Class<?> build() {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(originalClass)
                .name(originalClass.getName())
                .annotateType(
                        AnnotationDescription.Builder
                                .ofType(JsonAutoDetect.class)
                                .define("fieldVisibility", JsonAutoDetect.Visibility.ANY)
                                .build()
                );

        for (EAVField field : EAVs) {
            builder = addFieldWithAccessors(builder, field);
        }

        builder = overrideOriginalGetters(builder);

        Class<?> clazz = loadClass(builder);

        UniversalManager.registerDynamicClass(originalClass, key, clazz);

        return clazz;
    }

    public void compile() {
        build();
    }

    private DynamicType.Builder<?> addFieldWithAccessors(DynamicType.Builder<?> builder, EAVField field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String getterName = "get" + capitalize(fieldName);
        String setterName = "set" + capitalize(fieldName);

        builder = builder.defineField(fieldName, fieldType, Modifier.PRIVATE);

        builder = builder.defineMethod(getterName, fieldType, Modifier.PUBLIC)
                .intercept(FieldAccessor.ofField(fieldName));

        builder = builder.defineMethod(setterName, void.class, Modifier.PUBLIC)
                .withParameters(fieldType)
                .intercept(FieldAccessor.ofField(fieldName));

        return builder;
    }

    private DynamicType.Builder<?> overrideOriginalGetters(DynamicType.Builder<?> builder) {
        for (Method method : originalClass.getDeclaredMethods()) {
            if (isGetter(method)) {
                String fieldName = getFieldNameFromGetter(method.getName());
                builder = builder
                        .method(ElementMatchers.named(method.getName()))
                        .intercept(FieldAccessor.ofField(fieldName));
            }
        }
        return builder;
    }

    private boolean isGetter(Method method) {
        return method.getName().startsWith("get") &&
                method.getParameterCount() == 0 &&
                !method.getReturnType().equals(Void.TYPE);
    }

    private String getFieldNameFromGetter(String getterName) {
        return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
    }

    private Class<?> loadClass(DynamicType.Builder<?> builder) {
        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            return unloaded.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
        }
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
