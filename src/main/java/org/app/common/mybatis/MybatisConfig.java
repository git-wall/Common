package org.app.common.mybatis;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.app.common.db.DataSourceUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class MybatisConfig {

    @Value("${mybatis.mapper.resources=classpath*:/mapper/*.xml}")
    private String pathMapper;

    private final Environment env;

    @Bean(name = "dataSourceMybatis")
    public DataSource dataSource() {
        HikariDataSource dataSource = DataSourceUtils.defaultDataSource();
        dataSource.setJdbcUrl(env.getProperty("mybatis.datasource.url"));
        dataSource.setUsername(env.getProperty("mybatis.datasource.username"));
        dataSource.setPassword(env.getProperty("mybatis.datasource.password"));
        dataSource.setDriverClassName(env.getProperty("mybatis.datasource.driverClassName"));
        DataSourceUtils.appendConfig(dataSource);
        return dataSource;
    }

    @Bean
    @ConditionalOnBean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceMybatis") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(pathMapper));
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    @ConditionalOnBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
