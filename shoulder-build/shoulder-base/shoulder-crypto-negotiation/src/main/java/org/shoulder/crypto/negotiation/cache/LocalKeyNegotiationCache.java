package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥协商结果缓存
 *
 * @author lym
 */
public class LocalKeyNegotiationCache implements KeyNegotiationCache {

    /**
     * 客户端缓存，key为对方应用标识
     */
    private static Map<String, KeyExchangeResult> clientKeyExchangeResultMap = new ConcurrentHashMap<>(8);

    /**
     * 作为服务方缓存，key为 xSessionId
     */
    private static Map<String, KeyExchangeResult> serverKeyExchangeResultMap = new ConcurrentHashMap<>(8);

    @Override
    public void put(@Nonnull String cacheKey, @Nonnull KeyExchangeResult keyExchangeResult, boolean asClient) {
        Map<String, KeyExchangeResult> keyExchangeResultMap = asClient ?
            clientKeyExchangeResultMap : serverKeyExchangeResultMap;
        keyExchangeResultMap.put(cacheKey, keyExchangeResult);
    }

    @Override
    @Nullable
    public KeyExchangeResult get(String cacheKey, boolean asClient) {
        Map<String, KeyExchangeResult> keyExchangeResultMap = asClient ?
            clientKeyExchangeResultMap : serverKeyExchangeResultMap;
        long now = System.currentTimeMillis();
        KeyExchangeResult cacheResult = keyExchangeResultMap.get(cacheKey);
        if (cacheResult == null) {
            // 不存在
            return null;
        }
        if (now > cacheResult.getExpireTime()) {
            // 过期，清理，并返回 null
            keyExchangeResultMap.remove(cacheKey);
            return null;
        } else {
            // 存在且未过期
            return cacheResult;
        }
    }


}
