package org.app.common.base.distinct;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface GenericComparable {
    default boolean equalsGeneric(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ExcludeFromComparison.class)) {
                    continue;
                }
                field.setAccessible(true);
                if (!Objects.equals(field.get(this), field.get(o))) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error comparing objects", e);
        }
    }

    default int hashCodeGeneric() {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            List<Object> values = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(ExcludeFromComparison.class)) {
                    field.setAccessible(true);
                    values.add(field.get(this));
                }
            }
            return Objects.hash(values.toArray());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error generating hash code", e);
        }
    }
}
