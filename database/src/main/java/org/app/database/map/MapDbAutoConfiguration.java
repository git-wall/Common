package org.app.database.map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapDbAutoConfiguration {

    private final MapDbProperties properties;

    public MapDb mapDb() {
        return new MapDb(properties);
    }
}
