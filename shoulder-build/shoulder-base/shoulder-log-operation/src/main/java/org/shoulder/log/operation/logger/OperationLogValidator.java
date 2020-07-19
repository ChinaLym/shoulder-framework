package org.shoulder.log.operation.logger;

import org.shoulder.log.operation.entity.OperationLogEntity;

/**
 * 日志记录前校验
 * @author lym
 */
public interface OperationLogValidator {
    /**
     * 校验失败将抛出 RuntimeException
     * @param log 操作日志
     */
    void validate(OperationLogEntity log);
}
