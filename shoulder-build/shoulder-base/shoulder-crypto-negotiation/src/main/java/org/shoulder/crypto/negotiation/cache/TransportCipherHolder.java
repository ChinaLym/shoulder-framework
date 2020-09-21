package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.springframework.web.client.ResponseExtractor;

/**
 * 保存线程变量 —— 传输加解密处理器。
 * 这种缓存只是由于生成和使用的代码不在同一个拦截器中，实际存在时间非常短暂
 *
 * @author lym
 */
public class TransportCipherHolder {

    /**
     * 发起请求 和 处理请求时，使用这个
     */
    private static ThreadLocal<TransportCipher> request = new ThreadLocal<>();

    /**
     * 接收响应 和 响应对方时，用这个
     *
     * @see ResponseExtractor
     */
    private static ThreadLocal<TransportCipher> response = new ThreadLocal<>();

    /**
     * 用于解密收到的请求 or 加密发送请求
     */
    public static void setRequestCipher(TransportCipher transportCipher) {
        request.set(transportCipher);
    }

    public static TransportCipher removeRequestCipher() {
        TransportCipher cipher = request.get();
        request.remove();
        return cipher;
    }

    /**
     * 用于加密响应 or 解密对方响应
     */
    public static void setResponseCipher(TransportCipher transportCipher) {
        response.set(transportCipher);
    }

    public static TransportCipher removeResponseCipher() {
        TransportCipher cipher = response.get();
        response.remove();
        return cipher;
    }

}
