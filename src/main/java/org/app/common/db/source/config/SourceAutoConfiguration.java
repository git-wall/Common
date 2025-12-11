package org.app.common.db.source.config;

import org.app.common.context.SpringContext;
import org.app.common.db.source.DataSourceProvider;
import org.app.common.db.source.EnableMultiDataSource;
import org.app.common.db.source.MultiDataSourceManager;
import org.app.common.db.source.SourceConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Auto-configuration for multi-datasource support
 */
@Configuration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
@Import(value = {DataSourceConfig.class, PropertiesConfig.class})
public class SourceAutoConfiguration {

    private SourceConfigType configType;

    @Autowired(required = false)
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributesMap = importMetadata.getAnnotationAttributes(EnableMultiDataSource.class.getName());
        Assert.notNull(attributesMap, "EnableMultiDataSource annotation attributes must not be null");
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributesMap);
        this.configType = attributes.getEnum("configType");
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiDataSourceManager multiDataSourceManager() {
        Class<? extends DataSourceProvider> sourceProviderClass = configType.getDataSourceProvider();
        DataSourceProvider sourceProvider = SpringContext.getBean(sourceProviderClass);
        return new MultiDataSourceManager(sourceProvider.loadDataSourceConfigs());
    }
}
