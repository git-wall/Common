package org.app.common.eav.v2;

import lombok.Getter;

import java.util.Objects;

@Getter
public class EAVField {
    private final String name;
    private final Class<?> type;
    private final Object defaultValue;

    public EAVField(String name, Class<?> type) {
        this(name, type, null);
    }

    public EAVField(String name, Class<?> type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EAVField eavField = (EAVField) obj;
        return Objects.equals(name, eavField.name) && Objects.equals(type, eavField.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
