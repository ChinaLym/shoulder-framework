package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将操作日志发送到 RabbitMQ 中，由对应消费者（日志中心）处理
 * 类似的，可以将日志发送至 activeMQ、RocketMQ、Kafka、Plusar 等消息中间件暂存
 *
 * @author lym
 */
public class RabbitMQOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQOperationLogger.class);

    /*private final AmqpTemplate rabbitmq;

    public JdbcOperationLogger(AmqpTemplate rabbitmq) {
        this.rabbitmq = rabbitmq;
    }
*/

    @Override
    protected void doLog(OperationLogDTO opLog) {
        // todo send to rabbitMQ
    }

}
