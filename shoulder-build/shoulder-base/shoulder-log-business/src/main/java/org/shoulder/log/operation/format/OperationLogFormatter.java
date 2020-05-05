package org.shoulder.log.operation.format;

import org.shoulder.log.operation.entity.OperationLogEntity;

/**
 * 操作日志格式
 *
 * @author lym
 */
public interface OperationLogFormatter {

    /**
     * 格式化
     * @param logEntity 待打印的日志
     * @return 格式化后的 string
     */
    String format(OperationLogEntity logEntity);
}
