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
     * 流量来源： appId / EX 外部
     */
    String TRAFFIC_SOURCE = "TRAFFIC_SOURCE";

    /**
     * 客户端集成标
     */
    String CLIENT_ID = "CLIENT_ID";

    /**
     * 当前环境：DEV / TEST / PRE / GRAY / PROD
     * 线下：DEV 包括 stable、dev
     * 线下：TEST 包括 SIT、UNION_SIT
     * 线上：PRE、GRAY、PROD 每个都有仿真、正式
     *
     * @deprecated 不放上下文，而是单独作为常量，启动时加载、包括部署region
     */
    String ENV = "ENV";

    /**
     * 当前错误码，后续可优化为错误码堆栈
     */
    String ERROR_CODE = "ERROR_CODE";

    /**
     * 租户标识
     */
    String TENANT = "tenant";

    /**
     * 服务名
     */
    String SERVICE = "SERVICE";
    /**
     * 方法名
     */
    String METHOD  = "METHOD";

    /**
     * 认证标识 Authentication / Principal (如 accessToken / userInfo)
     */
    String AUTHENTICATION = "Authentication";

    /**
     * 用户id
     */
    String USER_ID            = "user-id";
    /**
     * 用户名称
     */
    String USER_NAME          = "user-name";
    /**
     * 语言标识
     */
    String LOCALE             = "locale";
    /**
     * 要打在摘要日志的内容，map
     */
    String DIGEST_LOG_CONTENT = "DIGEST_LOG_CONTENT";
    /**
     * traceId
     */
    String HEADER_TRACE_ID    = "x-traceId";
    /**
     * traceId
     */
    String TRACE_ID           = "traceId";

    /**
     * 灰度发布版本号
     */
    String GRAY_VERSION = "app-version";

}
