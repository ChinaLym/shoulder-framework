package org.shoulder.log.operation.format;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.log.operation.entity.OperationLogEntity;

/**
 * 默认日志格式化（逗号分隔的键值对）
 * "key1":"v1","k2":"v2"
 *
 * @author lym
 */
public class OperationLogJsonFormatter implements OperationLogFormatter {

    @Override
    public String format(OperationLogEntity opLog){
        return JsonUtils.toJson(opLog);

    }
}
