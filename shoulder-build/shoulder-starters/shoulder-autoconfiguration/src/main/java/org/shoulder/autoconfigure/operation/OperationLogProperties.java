package org.shoulder.autoconfigure.operation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 操作日志相关配置
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = "shoulder.log.operation")
public class OperationLogProperties {

    /**
     * 日志记录器配置
     */
    private LoggerProperties logger = new LoggerProperties();

    /**
     * 参数值为 null 时的输出样式，一般取值为 '' 或 'null' 等
     */
    private String nullParamOutput = "null";

    /**
     * 用户信息拦截器，顺序，若自定义用户信息，且较晚才能获取到，则适当调大
     */
    private Integer interceptorOrder = 0;


    @Data
    public static class LoggerProperties {

        /**
         * 操作日志记录方式
         */
        private PersistenceType type = PersistenceType.LOGGER;

        /**
         * 是否以异步线程记录 操作日志.
         */
        private boolean async = true;

        /**
         * 用于异步记录日志的线程数
         */
        private Integer threadNum = 1;

        /**
         * 异步记录日志线程的名称
         */
        private String threadName = "shoulder-async-operation-logger";


        public Integer getThreadNum() {
            return threadNum != null && threadNum > 0 ? threadNum : 1;
        }

    }

    public enum PersistenceType {

        /**
         * 保存到日志文件，如 lockBack 记录
         */
        LOGGER,

        /**
         * 保存到数据库
         */
        JDBC,

        /**
         * 保存到消息队列，RabbitMQ
         */
        RABBITMQ,

        /**
         * 保存到消息队列，Kafka
         */
        KAFKA,

        /**
         * 调用日志服务接口
         */
        HTTP,
        ;

    }
}
