package org.shoulder.crypto.negotiation.constant;


/**
 * 密钥交换相关常量
 *
 * @author lym
 */
public class NegotiationConstants {

    /**
     * 请求头命名前缀，X 为 http 协议规范中推荐的自定义前缀，S 代表 shoulder，x 代表安全
     */
    private static final String SHOULDER_SECURITY_HTTP_HEADER_PREFIX = "X-S-x";

    /**
     * 请求head中的会话标识
     */
    public static final String SECURITY_SESSION_ID = SHOULDER_SECURITY_HTTP_HEADER_PREFIX + "SessionID";
    /**
     * 请求head中的token，（签名）
     */
    public static final String TOKEN = SHOULDER_SECURITY_HTTP_HEADER_PREFIX + "Token";
    /**
     * 请求head中的数据密钥密文
     */
    public static final String SECURITY_DATA_KEY = SHOULDER_SECURITY_HTTP_HEADER_PREFIX + "DK";

    /**
     * 服务器缓存密钥过期头部响应标识
     * 目前仅用于服务端响应时携带暗示客户端删除密钥交换缓存，重新握手
     */
    public static final String NEGOTIATION_INVALID_TAG = "Negotiation-Invalid-Tag";


    /**
     * 协商的过期时间
     * 30min
     */
    public static final Integer EXPIRE_TIME = 30 * 60 * 1000;

    /**
     * 默认的密钥协商接口
     */
    public static final String DEFAULT_NEGOTIATION_URL = "/api/security/v1/negotiation";


}
