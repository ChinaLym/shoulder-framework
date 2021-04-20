package org.shoulder.autoconfigure.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;
import java.util.List;
/**
 * LettuceEventConsumer
 *
 * @author lym
 */
@Configuration
@ConditionalOnBean(LettuceEventConsumer.class)
public class LettuceEventConsumerConfiguration {

    @Bean
    public LettuceEventConsumerManager lettuceEventConsumerManager(@Nullable List<LettuceEventConsumer> consumers) {
        return new LettuceEventConsumerManager(consumers);
    }

    @Bean
    @ConditionalOnClass(RedisClient.class)
    @ConditionalOnBean(ClientResources.class)
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(ClientResources clientResources, LettuceEventConsumerManager lettuceEventConsumerManager) {
        clientResources.eventBus().get().subscribe(lettuceEventConsumerManager);
        return clientConfigurationBuilder -> clientConfigurationBuilder.clientResources(clientResources);
    }

}
