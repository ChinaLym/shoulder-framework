package org.shoulder.crypto.negotiation.constant;


/**
 * 密钥交换相关常量
 *
 * @author lym
 */
public class KeyExchangeConstants {

    /**
     * 服务端存储安全会话信息缓存的key前缀
     */
    public static final String SERVER_CACHE_PREFIX = "server.";
    /**
     * 客户端存储安全会话信息缓存的key前缀
     */
    public static final String CLIENT_CACHE_PREFIX = "client.";

    /**
     * 请求head中的会话标识
     */
    public static final String SECURITY_SESSION_ID = "xSessionId";
    /**
     * 请求head中的token，（签名）
     */
    public static final String TOKEN = "token";
    /**
     * 请求head中的数据密钥密文
     */
    public static final String SECURITY_DATA_KEY = "xDk";

    /**
     * 协商的过期时间
     * 30min
     */
    public static final Integer EXPIRE_TIME = 30 * 60 * 1000;

    /**
     * 默认的密钥协商接口
     */
    public static final String DEFAULT_NEGOTIATION_URL = "/security/v1/negotiation";


}
