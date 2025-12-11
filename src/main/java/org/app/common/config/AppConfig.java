package org.app.common.config;

import org.app.common.entities.ContactService;
import org.app.common.entities.DefaultContact;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfiguration
public class AppConfig {

    @Bean(initMethod = "init")
    public ContactService contactService() {
        return new DefaultContact();
    }
}
