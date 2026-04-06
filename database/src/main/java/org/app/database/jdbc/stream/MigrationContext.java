package org.app.database.jdbc.stream;

import lombok.Data;

import javax.sql.DataSource;
import java.util.Map;

@Data
public class MigrationContext {
    private final DataSource source;
    private final DataSource target;
    private final Map<String, Object> params;
}
