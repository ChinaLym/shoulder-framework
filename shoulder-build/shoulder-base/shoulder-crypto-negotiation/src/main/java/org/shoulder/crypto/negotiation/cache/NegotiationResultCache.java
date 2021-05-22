package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.dto.NegotiationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 密钥协商结果缓存
 *
 * @author lym
 */
public interface NegotiationResultCache {

    // =========== 线程缓存，在 getAsXxx 时放缓存，在处理响应时要从这里拿，因为在响应过程中密钥可能会恰巧过期 ==============

    /**
     * 客户端：接收响应从这里拿，避免等待接口返回值时密钥过期
     */
    ThreadLocal<NegotiationResult> CLIENT_LOCAL_CACHE = new ThreadLocal<>();

    /**
     * 服务端返回响应从这里拿，避免处理过程中密钥过期
     */
    ThreadLocal<NegotiationResult> SERVER_LOCAL_CACHE = new ThreadLocal<>();

    // ================= 放缓存，【只在密钥协商阶段调用】 一般使用封装的 putAsXXX ，可读性更好 ======================

    /**
     * 放缓存
     *
     * @param cacheKey          缓存 key
     * @param negotiationResult 密钥协商结果
     * @param asClient          角色
     */
    void put(@Nonnull String cacheKey, @Nonnull NegotiationResult negotiationResult, boolean asClient);


    /**
     * 子类实现该方法以实现获取缓存
     *
     * @param cacheKey 缓存 key
     * @param asClient 角色
     * @return KeyExchangeResult，过期或者不存在返回 null
     */
    @Nullable
    NegotiationResult get(String cacheKey, boolean asClient);

    // ---------------- putAsXXX -------------------

    /**
     * 放缓存
     *
     * @param appId             目标服务的应用标识（appId）
     * @param negotiationResult 密钥协商结果
     */
    default void putAsClient(@Nonnull String appId, @Nonnull NegotiationResult negotiationResult) {
        put(appId, negotiationResult, true);
    }

    /**
     * 放缓存
     *
     * @param xSessionId        客户端发来请求的 xSessionId
     * @param negotiationResult 密钥协商结果
     */
    default void putAsServer(@Nonnull String xSessionId, @Nonnull NegotiationResult negotiationResult) {
        put(xSessionId, negotiationResult, false);
    }

    // ============= 从缓存中拿数据，【应只在请求前获取，处理响应应从线程变量中获取，避免中途过期情况】 ============

    /**
     * 从缓存中拿数据，发送安全请求时
     *
     * @param appId 应用标识
     * @return 密钥协商结果 如果没有则为 null
     */
    @Nullable
    default NegotiationResult getAsClient(String appId) {
        return get(appId, true);
    }

    /**
     * 从缓存中拿数据，当接收对方安全请求时【应只在请求前获取，处理响应应从线程变量中获取，避免中途过期情况】
     *
     * @param xSessionId 客户端发来请求的 xSessionId
     * @return 密钥协商结果 如果没有则为 null
     */
    @Nullable
    default NegotiationResult getAsServer(String xSessionId) {
        return get(xSessionId, false);
    }


    // ============= 删除某个缓存，用于客户端处理服务方响应提示密钥过期 ============

    /**
     * 删除某个缓存
     *
     * @param cacheKey 缓存 key
     * @param asClient 角色
     */
    void delete(String cacheKey, boolean asClient);

}
