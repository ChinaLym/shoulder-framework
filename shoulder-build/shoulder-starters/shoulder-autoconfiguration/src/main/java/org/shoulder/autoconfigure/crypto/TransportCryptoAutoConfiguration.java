package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.asymmetric.AsymmetricCipher;
import org.shoulder.crypto.negotiation.algorithm.DelegateNegotiationAsymmetricCipher;
import org.shoulder.crypto.negotiation.cache.LocalNegotiationResultCache;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.cache.RedisNegotiationResultCache;
import org.shoulder.crypto.negotiation.support.endpoint.NegotiationEndPoint;
import org.shoulder.crypto.negotiation.support.server.SensitiveRequestDecryptAdvance;
import org.shoulder.crypto.negotiation.support.server.SensitiveResponseEncryptAdvice;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.support.service.impl.TransportNegotiationServiceImpl;
import org.shoulder.crypto.negotiation.util.TransportCryptoByteUtil;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * 传输加密自动配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TransportNegotiationService.class)
@AutoConfigureAfter(value = {LocalCryptoAutoConfiguration.class, HttpAutoConfiguration.class})
@ConditionalOnProperty(value = "shoulder.crypto.transport.enable", havingValue = "true", matchIfMissing = true)
public class TransportCryptoAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TransportCryptoAutoConfiguration.class);


    /**
     * 密钥协商工具，封装密钥协商相关基本方法单元
     *
     * @param delegate 用于处理协商的非对称加解密实现，默认使用当前项目中提供的非对称加密器
     */
    @Bean
    @ConditionalOnMissingBean
    public TransportCryptoUtil transportCryptoUtil(AsymmetricCipher delegate) {
        TransportCryptoByteUtil util = new TransportCryptoByteUtil(new DelegateNegotiationAsymmetricCipher(delegate));
        return new TransportCryptoUtil(util);
    }

    /**
     * 默认的密钥协商主要流程块
     */
    @Bean
    public TransportNegotiationService transportNegotiationService(TransportCryptoUtil transportCryptoUtil,
                                                                   RestTemplate restTemplate, NegotiationResultCache negotiationResultCache, AppIdExtractor appIdExtractor) {
        return new TransportNegotiationServiceImpl(transportCryptoUtil, restTemplate, negotiationResultCache, appIdExtractor);
    }

    /**
     * 密钥协商结果缓存（支持集群）
     */
    @ConditionalOnMissingBean(value = NegotiationResultCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    @ConditionalOnCluster
    @ConditionalOnClass(RestTemplate.class)
    public static class KeyNegotiationCacheClusterAutoConfiguration {

        @Bean
        public NegotiationResultCache redisKeyNegotiationCache(RedisTemplate<String, Object> redisTemplate) {

            RedisNegotiationResultCache keyNegotiationCache = new RedisNegotiationResultCache(redisTemplate);
            log.info("KeyNegotiationCache-redis init.");
            return keyNegotiationCache;
        }
    }

    /**
     * 密钥协商结果缓存（内存缓存）
     */
    @ConditionalOnMissingBean(value = NegotiationResultCache.class)
    @AutoConfigureAfter(RedisAutoConfiguration.class)
    @ConditionalOnCluster(cluster = false)
    public static class KeyNegotiationCacheLocalAutoConfiguration {

        @Bean
        public NegotiationResultCache localKeyNegotiationCache() {
            return new LocalNegotiationResultCache();
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
