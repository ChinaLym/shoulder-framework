package org.shoulder.log.operation.format.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.model.OperationLogDTO;

/**
 * JSON 日志格式化，以 Json 格式输出
 *
 * @author lym
 */
public class JsonOperationLogFormatter implements OperationLogFormatter {

    @Override
    public String format(OperationLogDTO opLog) {
        return JsonUtils.toJson(opLog);

    }
}
