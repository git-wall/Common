package org.app.database.jdbc.stream;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgresCopySink implements SinkWriter<InputStream> {

    private final CopyManager copyManager;
    private final PipedOutputStream out = new PipedOutputStream();
    private final PipedInputStream in = new PipedInputStream(out);

    public PostgresCopySink(Connection conn, String copySql) throws SQLException, IOException {
        this.copyManager = new CopyManager(conn.unwrap(BaseConnection.class));

        new Thread(() -> {
            try {
                copyManager.copyIn(copySql, in);
            } catch (Exception e) {
                throw new CopyException(e);
            }
        }).start();
    }

    @Override
    public void write(InputStream row) throws Exception {
        row.transferTo(out);
    }

    @Override
    public void flush() throws Exception {
        out.flush();
    }

    @Override
    public void close() throws Exception {
        out.close();
    }

    public static class CopyException extends RuntimeException {
        private static final long serialVersionUID = -6151718322524706028L;

        public CopyException(Exception e) {
            super(e);
        }
    }
}

