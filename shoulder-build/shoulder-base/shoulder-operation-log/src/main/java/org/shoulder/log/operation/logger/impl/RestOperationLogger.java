package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 将操作日志通过 Restful 接口发送给日志中心
 *
 * @author lym
 */
public class RestOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(RestOperationLogger.class);

    private final RestTemplate restTemplate;

    public RestOperationLogger(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doLog(OperationLogDTO opLog) {
        // todo send to logCenter
    }

}
