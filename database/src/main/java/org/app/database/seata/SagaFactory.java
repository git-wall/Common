package org.app.database.seata;

import io.seata.rm.datasource.DataSourceProxy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SagaFactory {

    public static DataSourceProxy buildDataSource(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }
}
