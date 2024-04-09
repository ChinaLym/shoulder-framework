package org.shoulder.core.log;

/**
 * 应用日志内置定义，使用者可以继承该接口继续扩展
 * <p>
 * 不使用枚举？便于扩展
 *<p>
 * @author lym
 */
public interface AppLoggers {

    /**
     * 运维日志：APP启动/重启等时逻辑
     * 默认包路径日志打在这里
     */
    Logger APP_DEFAULT = LoggerFactory.getLogger("APP-DEFAULT");

    /**
     * 运维日志：APP启动/重启等运维时逻辑
     */
    Logger APP_WARN = LoggerFactory.getLogger("APP-WARN");

    /**
     * 所有需要关注的 error 日志都在这里出现
     * 预期正常运行该日志内容为空
     */
    Logger APP_ERROR = LoggerFactory.getLogger("APP-ERROR");

    /**
     * 业务逻辑处理过程
     */
    Logger APP_BIZ = LoggerFactory.getLogger("APP-BIZ");

    /**
     * 业务路由，新老链路/逻辑切流日志：决策因子、规则、切流结果
     */
    Logger APP_ROUTER = LoggerFactory.getLogger("APP-ROUTER");
    /**
     * 调用其他应用 / 服务的入参、出参、错误码等详细信息
     */
    Logger APP_INTEGRATION = LoggerFactory.getLogger("APP-INTEGRATION");
    /**
     * 调用其他应用 / 服务的接口、方法、成功、耗时等关键信息
     */
    Logger APP_INTEGRATION_DIGEST = LoggerFactory.getLogger("APP-INTEGRATION-DIGEST");
    /**
     * 对外暴露接口/服务的日志详情，入参、出参等
     */
    Logger APP_SERVICE = LoggerFactory.getLogger("APP-SERVICE");
    /**
     * 对外暴露接口/服务的接口、方法、成功、耗时等关键信息
     */
    Logger APP_SERVICE_DIGEST = LoggerFactory.getLogger("APP-SERVICE-DIGEST");
    /**
     * 对外暴露接口/服务的接口执行出现预期内异常记录（参数校验失败等）
     */
    Logger APP_SERVICE_WARN = LoggerFactory.getLogger("APP-SERVICE-WARN");
    /**
     * 对外暴露接口/服务的接口执行失败记录
     */
    Logger APP_SERVICE_ERROR = LoggerFactory.getLogger("APP-SERVICE-ERROR");
    /**
     * 数据访问层-详细日志
     */
    Logger APP_DAL = LoggerFactory.getLogger("APP-DAL");
    /**
     * 数据访问层-摘要日志
     */
    Logger APP_DAL_DIGEST = LoggerFactory.getLogger("APP-DAL-DIGEST");
    /**
     * 数据访问层-缓存日志
     */
    Logger APP_CAL = LoggerFactory.getLogger("APP-CAL");
    /**
     * 数据访问层-缓存-摘要日志
     */
    Logger APP_CAL_DIGEST = LoggerFactory.getLogger("APP-CAL");
    /**
     * 定时任务触发记录
     */
    Logger APP_DAEMON = LoggerFactory.getLogger("APP-DAEMON");
    /**
     * 消费消息记录，如消息内容，消息来源等
     */
    Logger APP_MSG_CONSUMER = LoggerFactory.getLogger("APP-MSG-CONSUMER");
    /**
     * 发布消息记录，如消息内容等
     */
    Logger APP_MSG_PRODUCER = LoggerFactory.getLogger("APP-MSG-PRODUCER");
    /**
     * 向其他系统发送通知，如外部
     */
    Logger APP_NOTIFY = LoggerFactory.getLogger("APP-NOTIFY");
    /**
     * 收到配置变更
     * 读取到配置中心的配置内容
     */
    Logger APP_CONFIG = LoggerFactory.getLogger("APP-CONFIG");
    /**
     * 错误码映射记录，如调用 x 应用返回 errorCode_123，对于本服务接口应该返回 errorCode_567 时记录
     */
    Logger APP_CODE_MAPPING = LoggerFactory.getLogger("APP-CODE-MAPPING");

}
