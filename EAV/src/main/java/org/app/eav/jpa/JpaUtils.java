package org.app.eav.jpa;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

public class JpaUtils {

    public static final int TIMEOUT_DEFAULT = 30; // 30s

    public static EntityManagerFactory buildEntityManagerFactory(DataSource dataSource,
                                                                 String persistenceUnitName,
                                                                 String packageNameEntity,
                                                                 boolean generateDdl) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(generateDdl);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(packageNameEntity);
        factory.setPersistenceUnitName(persistenceUnitName);
        factory.setDataSource(dataSource);
        factory.setJpaPropertyMap(new HashMap<>());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    public static PlatformTransactionManager buildTransactionManager(EntityManagerFactory entityManagerFactory) {
        return buildTransactionManager(entityManagerFactory, TIMEOUT_DEFAULT);
    }

    public static PlatformTransactionManager buildTransactionManager(EntityManagerFactory entityManagerFactory, int timeout) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        txManager.setDefaultTimeout(timeout);
        return txManager;
    }
}
