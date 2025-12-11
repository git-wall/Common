package org.app.common.jackson.append;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

@Configuration
public class MixinConfig implements ImportAware {

    @Getter
    private Class<?> mixinClass;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attrs = importMetadata.getAnnotationAttributes(EnableMixin.class.getName());
        if (attrs != null) {
            this.mixinClass = (Class<?>) attrs.get("mixin");
        }
    }

    @Bean("MixinJackson")
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(ObjectMapper baseMapper) {
        if (mixinClass != null) {
            baseMapper.addMixIn(Object.class, mixinClass);
        }
        return baseMapper;
    }
}
