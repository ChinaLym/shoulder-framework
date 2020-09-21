package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.springframework.web.client.ResponseExtractor;

/**
 * 保存线程变量 —— 传输加解密处理器。这种缓存只是单纯的在不同的拦截器中使用，存在时间非常短暂
 * <p>
 * 两种思路，按照加解密/请求响应分
 * - 具体分
 * todo 注意清理线程变量
 * todo 处理多重请求：A -> B -> C，使用 Stack ？
 * - 对于服务端，responseCipher 可能会因为调用其他安全接口时覆盖
 *
 * @author lym
 */
public class TransportCipherHolder {

    /**
     * 发起请求 和 处理请求时，使用这个
     */
    private static ThreadLocal<TransportCipher> request = new ThreadLocal<>();


    /**
     * 用于解密收到的请求 or 加密发送请求
     */
    public static void setRequestCipher(TransportCipher transportCipher) {
        request.set(transportCipher);
    }

    /**
     * 接收响应 和 响应对方时，用这个
     *
     * @see ResponseExtractor
     */
    private static ThreadLocal<TransportCipher> response = new ThreadLocal<>();

    public static TransportCipher removeRequestCipher() {
        TransportCipher cipher = request.get();
        request.remove();
        return cipher;
    }

    public static TransportCipher removeResponseCipher() {
        TransportCipher cipher = response.get();
        response.remove();
        return cipher;
    }

    /**
     * 用于加密响应 or 解密对方响应
     */
    public static void setResponseCipher(TransportCipher transportCipher) {
        response.set(transportCipher);
    }

}
