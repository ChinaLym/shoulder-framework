package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 密钥协商结果缓存
 * @author lym
 */
public interface KeyNegotiationCache {

    /** todo 请求发送前set，
     * todo 不要直接使用
     * 接收响应前getAndRemove */
    ThreadLocal<KeyExchangeResult> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 放缓存
     * @param serviceId 对方服务标识
     * @param keyExchangeResult  密钥协商结果
     */
    default void putAsClient(@NonNull String serviceId, @NonNull KeyExchangeResult keyExchangeResult){
        put(serviceId, keyExchangeResult, true);
    }
    
    /**
     * 放缓存
     * @param xSessionId    客户端发来请求的 xSessionId
     * @param keyExchangeResult  密钥协商结果
     */
    default void putAsServer(@NonNull String xSessionId, @NonNull KeyExchangeResult keyExchangeResult){
        put(xSessionId, keyExchangeResult, false);
    }

    /**
     * 从缓存中拿数据，发送安全请求时
     * @param serviceId 服务标识
     * @return 密钥协商结果 如果没有则为 null
     */
    @Nullable
    default KeyExchangeResult getAsClient(String serviceId) {
        return get(serviceId, true);
    }

    /**
     * 从缓存中拿数据，当接收对方安全请求时
     * @param xSessionId 客户端发来请求的 xSessionId
     * @return 密钥协商结果 如果没有则为 null
     */
    @Nullable
    default KeyExchangeResult getAsServer(String xSessionId) {
        return get(xSessionId, false);
    }


    // ------------------------------------------------------------------------------

    /**
     * 放缓存
     * @param cacheKey  缓存 key
     * @param keyExchangeResult  密钥协商结果
     * @param asClient  角色
     */
    void put(@NonNull String cacheKey, @NonNull KeyExchangeResult keyExchangeResult, boolean asClient);


    /**
     *  子类实现该方法以实现获取缓存
     * @param cacheKey  缓存 key
     * @param asClient  角色
     * @return  KeyExchangeResult，过期或者不存在返回 null
     */
    @Nullable
    KeyExchangeResult get(String cacheKey, boolean asClient);



}
