package com.example.demo5.config;

import org.shoulder.crypto.local.repository.impl.FileLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.RedisLocalCryptoInfoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CryptoConfig 加密相关配置
 *
 * @author lym
 */
@Configuration
public class CryptoConfig {

    /**
     * fixme 这里使用了用于demo测试的 HashMapCryptoInfoRepository（每次运行将重置！）
     * 生产环境需要配置为可持久化的，如使用 mysql 作为持久化！！！！！
     *
     * @see JdbcLocalCryptoInfoRepository
     * @see RedisLocalCryptoInfoRepository
     * @see FileLocalCryptoInfoRepository
     */
    @Bean
    public HashMapCryptoInfoRepository hashMapCryptoInfoRepository() {
        return new HashMapCryptoInfoRepository();
    }

}
