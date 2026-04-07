package org.app.common.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Value("${redis.listener.pattern}")
    private String pattern;

    // custom class for your logic then to implement MessageListener
    @Bean
    @ConditionalOnBean({RedisConnectionFactory.class, MessageListener.class})
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListener listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(pattern));
        return container;
    }
}

