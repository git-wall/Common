package org.app.common.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtils {

    /**
     * Get set class impl by interface class
     * @param clazz interface class
     * */
    public static <T> Set<Class<? extends T>> getClasses(Class<T> clazz) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        return reflections.getSubTypesOf(clazz);
    }

    public static <T> T beanInstance(Class<T> clazz) {
        return BeanUtils.instantiateClass(clazz);
    }

    public static <T extends Serializable> T deepCopy(T t) {
        return SerializationUtils.clone(t);
    }

    public static String getClassName(Object obj) {
        return obj.getClass().getSimpleName();
    }

    @SneakyThrows
    public static Class<?> getClassByName(String className) {
        return Class.forName(className);
    }

    @SneakyThrows
    public static Class<?> getClassField(Object obj, String fieldName) {
        return obj.getClass().getDeclaredField(fieldName).getType();
    }

    @SneakyThrows
    public static Object invokeGetMethod(Object oj, String field) {
        String fieldName = String.format("get%s", org.apache.commons.lang3.StringUtils.capitalize(field));
        return MethodUtils.invokeMethod(oj, fieldName);
    }

    @SneakyThrows
    public static Object invokeGetMethod(Object oj, Field field) {
        String fieldName = String.format("get%s", field.getName());
        return MethodUtils.invokeMethod(oj, fieldName);
    }

    @SneakyThrows
    public static void invokeSetMethod(Object oj, String field, Object fieldValue) {
        String fieldName = String.format("set%s", org.apache.commons.lang3.StringUtils.capitalize(field));
        MethodUtils.invokeMethod(oj, fieldName, fieldValue);
    }

    @SneakyThrows
    public static void invokeSetMethod(Object oj, Field field, Object fieldValue) {
        String fieldName = String.format("set%s", field.getName());
        MethodUtils.invokeMethod(oj, fieldName, fieldValue);
    }

    public static List<String> getClassFieldNames(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return java.util.Arrays.stream(fields)
            .map(Field::getName)
            .collect(Collectors.toList());
    }
}
