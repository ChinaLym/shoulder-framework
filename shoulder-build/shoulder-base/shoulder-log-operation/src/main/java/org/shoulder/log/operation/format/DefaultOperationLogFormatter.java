package org.shoulder.log.operation.format;

import org.shoulder.log.operation.entity.OperationLogEntity;

/**
 * 默认日志格式化
 *
 * @author lym
 */
public class DefaultOperationLogFormatter implements OperationLogFormatter {
    
    @Override
    public String format(OperationLogEntity logEntity){
        return logEntity.toString();
    }
}
