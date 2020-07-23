package org.shoulder.log.operation.logger.intercept;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.format.OperationLogValidator;

/**
 * 用于对日志格式由一定要求时，与{@link OperationLogValidator} 配合使用在记录前进行校验
 *
 * @author lym
 */
public class OpLogValidateInterceptor implements OperationLoggerInterceptor {

    private final OperationLogValidator operationLogValidator;

    public OpLogValidateInterceptor(OperationLogValidator operationLogValidator) {
        this.operationLogValidator = operationLogValidator;
    }

    @Override
    public void beforeLog(OperationLogDTO opLog){
        operationLogValidator.validate(opLog);
    }

}
