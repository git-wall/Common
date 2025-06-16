package org.app.eav.contain;

import lombok.Getter;

@Getter
public enum DataSourceType {
    DATABASE("Database"),
    API("API");

    private final String type;

    DataSourceType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
