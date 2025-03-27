package org.app.common.base.cdc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Operation {

    READ("r"),
    CREATE("c"),
    UPDATE("u"),
    DELETE("d");

    private final String name;

    Operation(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}