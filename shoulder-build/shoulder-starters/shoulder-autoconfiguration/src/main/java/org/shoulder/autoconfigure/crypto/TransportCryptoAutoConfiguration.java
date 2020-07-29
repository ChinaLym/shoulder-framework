package org.shoulder.autoconfigure.crypto;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.crypto.asymmetric.annotation.Ecc;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.LocalKeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.RedisKeyNegotiationCache;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.service.impl.TransportNegotiationServiceImpl;
import org.shoulder.crypto.negotiation.util.TransportCryptoByteUtil;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * 传输加密自动配置
 * @author lym
 */
@Slf4j
@Configuration
@ConditionalOnClass(TransportNegotiationService.class)
@AutoConfigureAfter(CryptoAutoConfiguration.class)
public class TransportCryptoAutoConfiguration {

    @ConditionalOnMissingBean(value = KeyNegotiationCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    public static class KeyNegotiationCacheAutoConfiguration {

        @Bean
        @ConditionalOnCluster
        public KeyNegotiationCache redisKeyNegotiationCache(RedisTemplate<String, Object> redisTemplate,
                                                            @Value("${spring.application.name}") String applicationName){

            RedisKeyNegotiationCache keyNegotiationCache = new RedisKeyNegotiationCache(redisTemplate, applicationName);
            log.info("KeyNegotiationCache-redis init.");
            return keyNegotiationCache;
        }

        @Bean
        @ConditionalOnCluster(cluster = false)
        public KeyNegotiationCache localKeyNegotiationCache(){
            return new LocalKeyNegotiationCache();
        }
    }


    @Bean
    @ConditionalOnMissingBean
    public TransportCryptoUtil transportCryptoUtilAdapter(@Nullable @Ecc AsymmetricCryptoProcessor eccAsymmetricProcessor, KeyPairCache keyPairCache){
        AsymmetricCryptoProcessor eccCryptoProcessor = eccAsymmetricProcessor;
        if(eccAsymmetricProcessor == null){
            eccCryptoProcessor = DefaultAsymmetricCryptoProcessor.Default.ecc256(keyPairCache);
        }
        TransportCryptoByteUtil util = new TransportCryptoByteUtil(eccCryptoProcessor);
        return new TransportCryptoUtil(util);
    }


    @Bean
    public TransportNegotiationService transportNegotiationService(TransportCryptoUtil transportCryptoUtil, RestTemplate restTemplate, KeyNegotiationCache keyNegotiationCache){
        return new TransportNegotiationServiceImpl(transportCryptoUtil, restTemplate, keyNegotiationCache);
    }




}
