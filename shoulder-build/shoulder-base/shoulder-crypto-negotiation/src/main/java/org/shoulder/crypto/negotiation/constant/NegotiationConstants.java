package org.shoulder.crypto.negotiation.constant;


/**
 * 密钥交换相关常量
 *
 * @author lym
 */
public class NegotiationConstants {

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
     * 服务器缓存密钥过期头部响应标识
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
    public static final String DEFAULT_NEGOTIATION_URL = "/security/v1/negotiation";


}
