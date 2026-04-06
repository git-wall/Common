package org.app.database.jdbc.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class StreamContext {
    private final Connection connection;
    private final int fetchSize;
}
