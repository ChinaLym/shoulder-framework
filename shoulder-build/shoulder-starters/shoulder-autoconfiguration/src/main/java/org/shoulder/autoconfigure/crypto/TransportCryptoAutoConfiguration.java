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
import org.shoulder.crypto.negotiation.support.endpoint.NegotiationEndPoint;
import org.shoulder.crypto.negotiation.support.server.SensitiveRequestDecryptAdvance;
import org.shoulder.crypto.negotiation.support.server.SensitiveResponseEncryptAdvice;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.support.service.impl.TransportNegotiationServiceImpl;
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
import org.springframework.core.Ordered;
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

    /**
     * 密钥协商工具，封装密钥协商相关基本方法单元
     *
     * @param eccAsymmetricProcessor 需要有 ECC 的非对称加解密实现
     * @param keyPairCache           keyPair 缓存
     */
    @Bean
    @ConditionalOnMissingBean
    public TransportCryptoUtil transportCryptoUtil(@Nullable @Ecc AsymmetricCryptoProcessor eccAsymmetricProcessor, KeyPairCache keyPairCache) {
        AsymmetricCryptoProcessor eccCryptoProcessor = eccAsymmetricProcessor;
        if (eccAsymmetricProcessor == null) {
            eccCryptoProcessor = DefaultAsymmetricCryptoProcessor.ecc256(keyPairCache);
        }
        TransportCryptoByteUtil util = new TransportCryptoByteUtil(eccCryptoProcessor);
        return new TransportCryptoUtil(util);
    }

    /**
     * 默认的密钥协商主要流程块
     */
    @Bean
    public TransportNegotiationService transportNegotiationService(TransportCryptoUtil transportCryptoUtil,
                                                                   RestTemplate restTemplate, KeyNegotiationCache keyNegotiationCache, AppIdExtractor appIdExtractor) {
        return new TransportNegotiationServiceImpl(transportCryptoUtil, restTemplate, keyNegotiationCache, appIdExtractor);
    }

    /**
     * 密钥协商结果缓存（支持集群）
     */
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

    /**
     * 密钥协商结果缓存（内存缓存）
     */
    @ConditionalOnMissingBean(value = KeyNegotiationCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    @ConditionalOnCluster(cluster = false)
    public static class KeyNegotiationCacheLocalAutoConfiguration {

        @Bean
        public KeyNegotiationCache localKeyNegotiationCache() {
            return new LocalKeyNegotiationCache();
        }
    }

    /**
     * 密钥协商默认端点
     */
    @Bean
    @ConditionalOnProperty(value = "shoulder.crypto.negotiation.default-endpoint.enable", matchIfMissing = true)
    public NegotiationEndPoint negotiationEndPoint(TransportNegotiationService negotiationService) {
        return new NegotiationEndPoint(negotiationService);
    }

    /**
     * 请求解密
     */
    @Bean
    @Order(value = Ordered.HIGHEST_PRECEDENCE)// advance 越大越先执行
    @ConditionalOnClass(SensitiveRequestDecryptAdvance.class)
    public SensitiveRequestDecryptAdvance sensitiveRequestDecryptAdvance() {
        return new SensitiveRequestDecryptAdvance();
    }

    /**
     * 响应加密
     */
    @Bean
    @Order(value = 20)// advance 越大越先执行
    @ConditionalOnClass(SensitiveResponseEncryptAdvice.class)
    public SensitiveResponseEncryptAdvice sensitiveResponseEncryptAdvice(TransportCryptoUtil cryptoUtil) {
        return new SensitiveResponseEncryptAdvice(cryptoUtil);
    }

}
