package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.springframework.web.client.ResponseExtractor;

/**
 * 保存线程变量 —— 传输加解密处理器
 *
 * @author lym
 */
public class TransportCipherHolder {

    /**
     * 发起请求 和 处理请求时，使用这个
     */
    private static ThreadLocal<TransportCipher> request = new ThreadLocal<>();

    /**
     * 本次请求是否为加密接口
     */
    private static ThreadLocal<Boolean> negotiationApi = new ThreadLocal<>();

    /**
     * 接收响应 和 响应对方时，用这个
     * @see ResponseExtractor
     */
    private static ThreadLocal<TransportCipher> response = new ThreadLocal<>();

    public static TransportCipher getRequestCipher() {
        return request.get();
    }

    /**
     * 用于解密收到的请求 or 加密发送请求
     */
    private static void setRequestCipher(TransportCipher transportCipher) {
        request.set(transportCipher);
    }

    public static TransportCipher getResponseHandler() {
        return response.get();
    }

    /**
     * 用于请求发起端 加密
     * todo 需要一个特殊 restTemplate 或者拦截器中重新构造参数
     */
    public static void setRequestEncryptor(TransportCipher transportCipher) {
        setRequestCipher(transportCipher);
    }

    /**
     * 用于服务提供端 解密
     */
    public static void setRequestDecryptor(TransportCipher transportCipher) {
        setRequestCipher(transportCipher);
    }

    /**
     * 用于服务提供端 加密响应
     */
    public static void setResponseEncryptor(TransportCipher transportCipher) {
        setResponseCipher(transportCipher);
    }

    /**
     * 用于请求发起端 解密收到得响应
     */
    public static void setResponseDecryptor(TransportCipher transportCipher) {
        setResponseCipher(transportCipher);
    }

    /**
     * 用于加密响应 or 解密对方响应
     */
    private static void setResponseCipher(TransportCipher transportCipher) {
        response.set(transportCipher);
    }

    /**
     * todo 请求完成后应该清理
     */
    public static void cleanRequestDecryptHandler() {
        request.remove();
    }

    public static void cleanResponseCryptHandler() {
        response.remove();
    }

    public static boolean isNegotiationApi() {
        return negotiationApi.get();
    }

    public static void setNegotiationApi(boolean isNegotiationApi) {
        TransportCipherHolder.negotiationApi.set(isNegotiationApi);
    }

    /**
 * 请求状态，用于合成两个注解
 *//*

    private static ThreadLocal<Boolean> status = ThreadLocal.withInitial(() -> null);

    public static void cleanStatus() {
        status.remove();
    }

    */
/**
 * 标记为正在进行请求
 *//*

    public static void setRequestStatus() {
        status.set(true);
    }

    */
/**
 * 标记为正在进行处理响应
 *//*

    public static void setResponseStatus() {
        status.set(false);
    }
    */
/**
 * 现在是否处于请求阶段
 *//*

    public static boolean isRequesting() {
        return status.get() == null ? false : status.get();
    }
    */
/**
 * 现在是否处于响应阶段
 *//*

    public static boolean isResponsed() {
        return status.get() != null && !status.get();
    }
*/


}
