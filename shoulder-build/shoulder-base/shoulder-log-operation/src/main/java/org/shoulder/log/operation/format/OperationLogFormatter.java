package org.shoulder.log.operation.format;

import org.shoulder.log.operation.entity.OperationLogEntity;

/**
 * 操作日志格式化
 *  不同的系统中希望的日志格式不一，有的人希望是逗号分隔的键值对，如 nginx 默认日志格式，也有的人希望是 json 格式
 *  可以通过扩展该接口来修改输出格式
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
