package org.app.database.jdbc.stream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;

// Example with csv
public class CsvFileSink implements SinkWriter<ResultSet> {

    private final BufferedWriter writer;

    public CsvFileSink(Path file) throws IOException {
        this.writer = Files.newBufferedWriter(file);
    }

    @Override
    public void write(ResultSet rs) throws Exception {
        writer.write(
            rs.getLong("id") + "," +
                rs.getString("name") + "," +
                rs.getTimestamp("created_at")
        );
        writer.newLine();
    }

    @Override
    public void flush() throws Exception {
        writer.flush();
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
