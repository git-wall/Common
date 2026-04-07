package org.app.database.mybatis;

import com.zaxxer.hikari.HikariDataSource;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.app.database.utils.DataSourceUtils;

import javax.sql.DataSource;
import java.io.IOException;


@RequiredArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MybatisFactory {

    private String pathMapper = "classpath*:/mapper/*.xml";

    public DataSource dataSource(String url, String username, String password, String driverClassName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        DataSourceUtils.appendConfig(dataSource);
        return dataSource;
    }

    /**
     * Tạo SqlSessionFactory với XML mappers
     */
    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws IOException {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);

        // Thêm cấu hình
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(false);
        configuration.setAggressiveLazyLoading(false);

        // Load XML mappers từ classpath
        loadXmlMappers(configuration);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Load XML mappers từ thư mục
     */
    private void loadXmlMappers(Configuration configuration) {
        // Cách 1: Load từ classpath sử dụng ClassLoader
        String mapperPackage = pathMapper.replace('/', '.');
        configuration.addMappers(mapperPackage);

        // Cách 2: Load trực tiếp XML files (nếu cần)
        // Bạn có thể implement logic đọc XML files và add mapper
    }

    /**
     * Tạo SqlSessionFactory với annotation mappers
     */
    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Class<?>... mapperClasses) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);

        configuration.setMapUnderscoreToCamelCase(true);

        // Add mapper classes
        for (Class<?> mapperClass : mapperClasses) {
            configuration.addMapper(mapperClass);
        }

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Tạo SqlSession (không tự động commit)
     */
    public SqlSession openSession(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.openSession();
    }

    /**
     * Tạo SqlSession với auto commit
     */
    public SqlSession openSession(SqlSessionFactory sqlSessionFactory, boolean autoCommit) {
        return sqlSessionFactory.openSession(autoCommit);
    }

    /**
     * Lấy mapper từ SqlSession
     */
    public <T> T getMapper(SqlSession sqlSession, Class<T> mapperClass) {
        return sqlSession.getMapper(mapperClass);
    }

    /**
     * Set đường dẫn mapper
     */
    public void setPathMapper(String pathMapper) {
        this.pathMapper = pathMapper;
    }
}
