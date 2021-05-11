package org.shoulder.autoconfigure.endpoints;

import org.shoulder.core.guid.InstanceIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * endpoint
 *
 * @author lym
 */
@Configuration
public class InstanceIdEndpointAutoConfiguration {


    @Bean
    @ConditionalOnBean(InstanceIdProvider.class)
    public InstanceIdEndpoint instanceIdEndpoint(InstanceIdProvider instanceIdProvider) {
        return new InstanceIdEndpoint(instanceIdProvider);
    }


}
