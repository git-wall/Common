package org.app.database.jdbc.stream;

public interface MigrationJob {
    void run(MigrationContext ctx) throws Exception;
}
