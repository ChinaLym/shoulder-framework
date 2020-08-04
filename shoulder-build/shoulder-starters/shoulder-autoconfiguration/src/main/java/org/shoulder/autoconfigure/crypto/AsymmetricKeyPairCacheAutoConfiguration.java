package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.cluster.redis.annotation.ApplicationExclusive;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.RedisKeyPairCache;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 非对称加密缓存自动配置
 *
 * @author lym
 */
@Configuration
@ConditionalOnMissingBean(KeyPairCache.class)
@ConditionalOnClass(AsymmetricTextCipher.class)
@AutoConfigureAfter(LocalCryptoAutoConfiguration.class)
public class AsymmetricKeyPairCacheAutoConfiguration {


}
