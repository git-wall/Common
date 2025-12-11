package org.app.common.db.transaction;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.OptimisticLockException;
import java.util.Map;

@Configuration
public class TransactionSupportConfiguration implements ImportAware {

    private int defaultTimeout;

    @Bean
    @ConditionalOnBean(TransactionManager.class)
    public TransactionSupport transactionSupport(PlatformTransactionManager txManager) {
        TransactionSupport txSupport = new TransactionSupport(txManager);

        txSupport.registerTransaction(EnableTransactionSupport.Key.DEFAULT_TX,
            defaultTimeout,
            Isolation.READ_COMMITTED,
            Propagation.REQUIRED);

        txSupport.registerRetry(EnableTransactionSupport.Key.DEFAULT_RETRY,
            3, 100, 2, Map.of(OptimisticLockException.class, true));

        return txSupport;
    }

    public void setImportMetadata(AnnotationMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableTransactionSupport.class.getName());
        assert attrs != null;
        this.defaultTimeout = (int) attrs.get("defaultTimeout");
    }
}
