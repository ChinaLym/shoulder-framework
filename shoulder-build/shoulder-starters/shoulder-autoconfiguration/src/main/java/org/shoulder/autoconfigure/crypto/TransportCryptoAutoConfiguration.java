package org.shoulder.autoconfigure.crypto;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;
import org.shoulder.crypto.asymmetric.annotation.Ecc;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.LocalKeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.RedisKeyNegotiationCache;
import org.shoulder.crypto.negotiation.endpoint.NegotiationEndPoint;
import org.shoulder.crypto.negotiation.interceptor.SecurityRestControllerAutoCryptoResponseAdvice;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.service.impl.TransportNegotiationServiceImpl;
import org.shoulder.crypto.negotiation.util.TransportCryptoByteUtil;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * 传输加密自动配置
 *
 * @author lym
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TransportNegotiationService.class)
@AutoConfigureAfter(value = {LocalCryptoAutoConfiguration.class, HttpAutoConfiguration.class})
@ConditionalOnProperty(value = "shoulder.crypto.transport.enable", havingValue = "true", matchIfMissing = true)
public class TransportCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransportCryptoUtil transportCryptoUtilAdapter(@Nullable @Ecc AsymmetricCryptoProcessor eccAsymmetricProcessor, KeyPairCache keyPairCache) {
        AsymmetricCryptoProcessor eccCryptoProcessor = eccAsymmetricProcessor;
        if (eccAsymmetricProcessor == null) {
            eccCryptoProcessor = DefaultAsymmetricCryptoProcessor.ecc256(keyPairCache);
        }
        TransportCryptoByteUtil util = new TransportCryptoByteUtil(eccCryptoProcessor);
        return new TransportCryptoUtil(util);
    }

    @Bean
    public TransportNegotiationService transportNegotiationService(TransportCryptoUtil transportCryptoUtil,
                                                                   RestTemplate restTemplate, KeyNegotiationCache keyNegotiationCache, AppIdExtractor appIdExtractor) {
        return new TransportNegotiationServiceImpl(transportCryptoUtil, restTemplate, keyNegotiationCache, appIdExtractor);
    }

    @ConditionalOnMissingBean(value = KeyNegotiationCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    @ConditionalOnCluster
    @ConditionalOnClass(RestTemplate.class)
    public static class KeyNegotiationCacheClusterAutoConfiguration {

        @Bean
        public KeyNegotiationCache redisKeyNegotiationCache(RedisTemplate<String, Object> redisTemplate,
                                                            @Value("${spring.application.name}") String applicationName) {

            RedisKeyNegotiationCache keyNegotiationCache = new RedisKeyNegotiationCache(redisTemplate, applicationName);
            log.info("KeyNegotiationCache-redis init.");
            return keyNegotiationCache;
        }

    }

    @ConditionalOnMissingBean(value = KeyNegotiationCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    @ConditionalOnCluster(cluster = false)
    public static class KeyNegotiationCacheLocalAutoConfiguration {

        @Bean
        public KeyNegotiationCache localKeyNegotiationCache() {
            return new LocalKeyNegotiationCache();
        }
    }

    @Bean
    @ConditionalOnProperty(value = "shoulder.crypto.negotiation.default-endpoint.enable", matchIfMissing = true)
    public NegotiationEndPoint negotiationEndPoint(TransportNegotiationService negotiationService) {
        return new NegotiationEndPoint(negotiationService);
    }

    @Bean
    @Order(value = 20)// advance 越大越先执行
    @ConditionalOnClass(SecurityRestControllerAutoCryptoResponseAdvice.class)
    public SecurityRestControllerAutoCryptoResponseAdvice securityRestControllerAutoCryptoResponseAdvice(TransportCryptoUtil cryptoUtil) {
        return new SecurityRestControllerAutoCryptoResponseAdvice(cryptoUtil);
    }

}
