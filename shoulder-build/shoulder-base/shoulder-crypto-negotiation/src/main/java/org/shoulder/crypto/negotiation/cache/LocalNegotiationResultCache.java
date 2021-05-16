package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.dto.NegotiationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥协商结果缓存
 *
 * @author lym
 */
public class LocalNegotiationResultCache implements NegotiationResultCache {

    /**
     * 客户端缓存，key为对方应用标识
     */
    private static Map<String, NegotiationResult> clientKeyExchangeResultMap = new ConcurrentHashMap<>(8);

    /**
     * 作为服务方缓存，key为 xSessionId
     */
    private static Map<String, NegotiationResult> serverKeyExchangeResultMap = new ConcurrentHashMap<>(8);

    @Override
    public void put(@Nonnull String cacheKey, @Nonnull NegotiationResult negotiationResult, boolean asClient) {
        Map<String, NegotiationResult> keyExchangeResultMap = getKeyExchangeCacheMap(asClient);
        keyExchangeResultMap.put(cacheKey, negotiationResult);
    }

    @Override
    @Nullable
    public NegotiationResult get(String cacheKey, boolean asClient) {
        Map<String, NegotiationResult> keyExchangeResultMap = getKeyExchangeCacheMap(asClient);
        long now = System.currentTimeMillis();
        NegotiationResult cacheResult = keyExchangeResultMap.get(cacheKey);
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

    @Override
    public void delete(String cacheKey, boolean asClient) {
        Map<String, NegotiationResult> keyExchangeCache = getKeyExchangeCacheMap(asClient);
        keyExchangeCache.remove(cacheKey);
    }


    private Map<String, NegotiationResult> getKeyExchangeCacheMap(boolean asClient) {
        return asClient ? clientKeyExchangeResultMap : serverKeyExchangeResultMap;
    }

}
