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
     * 认证标识 Authentication / Principal (如 accessToken / userInfo)
     */
    String AUTHENTICATION = "Authentication";

    /**
     * 用户id
     */
    String USER_ID = "user-id";
    /**
     * 用户名称
     */
    String USER_NAME = "user-name";
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
    String TRACE_ID = "traceId";

    /**
     * 租户标识
     */
    String TENANT = "tenant";
    /**
     * 灰度发布版本号
     */
    String GRAY_VERSION = "app-version";


}
