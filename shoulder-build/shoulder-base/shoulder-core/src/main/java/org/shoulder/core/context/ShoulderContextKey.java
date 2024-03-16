package org.shoulder.core.context;

/**
 * shoulder 框架上下文中的 key
 *
 * @author lym
 */
public interface ShoulderContextKey {

    /**
     * 流量类型：生产 P、测试流量 T、仿真 S、压力测试 L...
     */
    String TRAFFIC_TYPE = "TRAFFIC_TYPE";

    /**
     * 当前环境：DEV / TEST / PRE / GRAY / PROD
     * 线下：DEV 包括 stable、dev
     * 线下：TEST 包括 SIT、UNION_SIT
     * 线上：PRE、GRAY、PROD 每个都有仿真、正式
     */
    String ENV = "ENV";

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
