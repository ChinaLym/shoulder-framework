package org.shoulder.log.operation.format;

import org.shoulder.log.operation.dto.OperationLogDTO;

/**
 * 日志校验器，若日志格式有一定要求则可以添加校验器。
 * 如希望存储到 db 时，对字段长度有要求，因此需要校验
 * 希望自定义系统中的日志格式，但担心开发者不按照规范走可以进行校验，如某些字段需要以特定前后缀结尾等
 *
 * @author lym
 */
public interface OperationLogValidator {

    /**
     * 校验失败将抛出 RuntimeException
     *
     * @param log 操作日志
     */
    void validate(OperationLogDTO log);
}
