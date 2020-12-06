package org.shoulder.crypto.negotiation.cache;

import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.support.client.SensitiveRequestEncryptMessageConverter;
import org.shoulder.crypto.negotiation.support.server.SensitiveResponseEncryptAdvice;
import org.springframework.lang.Nullable;
import org.springframework.web.client.ResponseExtractor;

/**
 * 传输加解密处理器 缓存
 * 这种缓存只是由于生成和使用的代码不在同一个拦截器中，实际存在时间非常短暂（线程变量）
 *
 * @author lym
 */
public class TransportCipherHolder {

    /**
     * 发起请求 和 处理请求时，都使用这个
     * todo 目前服务端收到加密通信请求后，必须先完全解密，才能对其他服务再发送请求，否则无法再次对本请求中的敏感数据解密（已经被复写）
     */
    private static ThreadLocal<TransportTextCipher> request = new ThreadLocal<>();

    /**
     * 作为客户端发起请求时，使用这个
     */
    private static ThreadLocal<DefaultTransportCipher> requestAsClientEncryptCipher = new ThreadLocal<>();

    /**
     * 作为客户端，调用外部服务，接收加密响应时用这个
     * 作为服务端，响应对方时不需要使用，因为写入响应时再随机生成密钥，无需提前生成以及缓存
     *
     * @see ResponseExtractor
     * @see SensitiveRequestEncryptMessageConverter#read 客户端使用 remove 方法
     * @see SensitiveResponseEncryptAdvice 服务端响应加密
     */
    private static ThreadLocal<TransportTextCipher> response = new ThreadLocal<>();

    /**
     * 用于解密收到的请求 or 加密发送请求
     */
    public static void setRequestCipher(TransportTextCipher transportCipher) {
        request.set(transportCipher);
    }

    /**
     * 如果没有放则可能为 null
     *
     * @return 加密/解密处理器
     */
    @Nullable
    public static TransportTextCipher removeRequestCipher() {
        TransportTextCipher cipher = request.get();
        request.remove();
        return cipher;
    }

    /**
     * 用于加密响应 or 解密对方响应
     */
    public static void setResponseCipher(TransportTextCipher transportCipher) {
        response.set(transportCipher);
    }

    public static TransportTextCipher removeResponseCipher() {
        TransportTextCipher cipher = response.get();
        response.remove();
        return cipher;
    }

}
