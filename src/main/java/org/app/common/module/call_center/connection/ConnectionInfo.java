package org.app.common.module.call_center.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores connection information with version
 * @param <T> The type of connection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionInfo<T> {
    private Integer version;
    private T connection;
}