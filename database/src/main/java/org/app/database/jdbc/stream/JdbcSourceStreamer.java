package org.app.database.jdbc.stream;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RequiredArgsConstructor
public class JdbcSourceStreamer implements SourceStreamer<ResultSet> {

    private final String sql;

    @Override
    public void stream(StreamContext ctx, RowHandler<ResultSet> handler) throws Exception {
        Connection conn = ctx.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(ctx.getFetchSize());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                handler.handle(rs);
            }
        }
    }
}
