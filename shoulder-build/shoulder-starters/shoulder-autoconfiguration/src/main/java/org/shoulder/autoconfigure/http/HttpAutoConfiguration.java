package org.shoulder.autoconfigure.http;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.http.ServiceIdExtractor;
import org.shoulder.http.ShoulderDslServiceIdExtractor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * http 相关配置
 * @author lym
 */
@Configuration
@ConditionalOnClass(ServiceIdExtractor.class)
public class HttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceIdExtractor serviceIdExtractor(){
        return new ShoulderDslServiceIdExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
