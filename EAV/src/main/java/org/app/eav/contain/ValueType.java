package org.app.eav.contain;

public enum ValueType {
    STRING("TEXT"),
    NUMBER("NUMBER"),
    DATE("DATE"),
    DATETIME("DATETIME"),
    TIME("TIME"),
    BOOLEAN("BOOLEAN"),
    JSON("json");

    private final String type;

    ValueType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
