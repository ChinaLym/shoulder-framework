package org.shoulder.core.context;

/**
 * shoulder 框架上下文中的 key
 *
 * @author lym
 */
public interface ShoulderContextKey {
    /**
     * 定义的 key 个数
     */
    int KEY_NUM = 9;

    /**
     * 认证标识 (accessToken)
     */
    String HEADER_TOKEN = "Authentication";

    /**
     * 用户id
     */
    String JWT_KEY_USER_ID = "uid";
    /**
     * 用户名称
     */
    String JWT_KEY_NAME = "name";
    /**
     * 用户账号
     */
    String JWT_KEY_ACCOUNT = "account";
    /**
     * 语言标识
     */
    String LOCALE = "locale";


    /**
     * traceId
     */
    String HEADER_TRACE_ID = "x-traceId";
    /**
     * traceId
     */
    String MDC_TRACE_ID = "traceId";

    /**
     * 租户标识
     */
    String TENANT = "tenant";
    /**
     * 灰度发布版本号
     */
    String GRAY_VERSION = "grayversion";


}
