package org.shoulder.autoconfigure.operation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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
         * TODO 操作日志记录方式
         */
        private PersistenceType type = PersistenceType.LOGGER;

        /**
         * 是否以异步线程记录 操作日志
         * 默认开启
         */
        private boolean async = true;

        /**
         * 用于异步记录日志的线程数
         */
        private Integer threadNum = 1;

        /**
         * 异步记录日志线程的名称
         */
        private String threadName = "shoulder-opLogger";

        /**
         * 是否启用缓冲池。优化频繁记录单条，如：需将操作日志存数据库/发送至远程可使用，开启后每 0.2s 插入一次数据库 -> 每隔一段时间批量插入数据库
         * 默认 false，开启后可能无法查看到实时操作日志
         */
        private boolean buffered = false;

        /**
         * buffer 日志记录器，每隔多少秒刷一次
         */
        private Duration flushInterval = Duration.ofSeconds(10);

        /**
         * 当积攒的 buffer 中日志数达到 flushThreshold 条触发一次批量记录，不影响固定扫描间隔
         */
        private Integer flushThreshold = 10;

        /**
         * 每次批量刷日志最大条数
         * 推荐根据实际情况定制。如统计 Mysql单页可以存几条数据，取该值作为单次保存量
         * 如调HTTP接口保存 / MQ保存，可以适量调大
         */
        private Integer perFlushMax = 20;


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
