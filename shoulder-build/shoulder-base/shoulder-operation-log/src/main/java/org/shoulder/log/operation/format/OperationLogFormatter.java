package org.shoulder.log.operation.format;

import org.shoulder.log.operation.logger.impl.LogOperationLogger;
import org.shoulder.log.operation.model.OperationLogDTO;

/**
 * 操作日志格式化
 * 不同的系统中希望的日志格式不一，有的希望是逗号分隔的键值对（如 nginx 默认日志格式），也有的人希望是 json 格式
 * 可以通过扩展该接口来修改输出格式
 *
 * @see LogOperationLogger 供打印日志记录器使用
 * @author lym
 */
public interface OperationLogFormatter {

    /**
     * 格式化
     *
     * @param opLog 待打印的日志
     * @return 格式化后的 string
     */
    String format(OperationLogDTO opLog);
}
