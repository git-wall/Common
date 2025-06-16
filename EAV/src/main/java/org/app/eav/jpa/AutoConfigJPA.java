package org.app.eav.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@EnableJpaRepositories(
        basePackages = {"${jpa.scan.basePackages:}"}
)
@Configuration
@EnableConfigurationProperties(value = {JpaProperties.class, JpaBaseProperties.class})
public class AutoConfigJPA {

    private final JpaProperties jpaProperties;

    private final JpaBaseProperties jpaBaseProperties;

    public AutoConfigJPA(JpaProperties jpaProperties, JpaBaseProperties jpaBaseProperties) {
        this.jpaProperties = jpaProperties;
        this.jpaBaseProperties = jpaBaseProperties;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        return JpaUtils.buildEntityManagerFactory(
                dataSource,
                jpaBaseProperties.getPersistenceUnitName(),
                jpaBaseProperties.getPackagesToScan(),
                jpaProperties.isGenerateDdl()
        );
    }

    @Bean
    @ConditionalOnBean(EntityManagerFactory.class)
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return JpaUtils.buildTransactionManager(entityManagerFactory, JpaUtils.TIMEOUT_DEFAULT);
    }
}
