package org.app.common.module.benchmark.config;

import org.app.common.module.benchmark.core.AppPackageResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BenchmarkAutoConfiguration {

    @Bean
    public AppPackageResolver appPackageResolver() {
        return new AppPackageResolver();
    }
}
