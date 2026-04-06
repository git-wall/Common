package org.app.common.redis;


import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Set;

@Configuration
public class WriteMasterReadReplicaConfig {

    @Bean
    @ConditionalOnBean(RedisProperties.class)
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisSentinelConfiguration sentinelConfig =
            new RedisSentinelConfiguration(
                redisProperties.getSentinel().getMaster(),
                Set.copyOf(redisProperties.getSentinel().getNodes())
            );

        String password = redisProperties.getPassword();
        if (password != null) {
            sentinelConfig.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

}
