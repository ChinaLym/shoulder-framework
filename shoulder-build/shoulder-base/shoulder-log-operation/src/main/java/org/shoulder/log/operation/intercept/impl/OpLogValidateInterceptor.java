package org.shoulder.log.operation.intercept.impl;

import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.OperationLogValidator;

/**
 * 在记录前进行校验
 * @author lym
 */
public class OpLogValidateInterceptor implements OperationLoggerInterceptor {

    private final OperationLogValidator operationLogValidator;

    public OpLogValidateInterceptor(OperationLogValidator operationLogValidator) {
        this.operationLogValidator = operationLogValidator;
    }

    @Override
    public void beforeLog(OperationLogEntity opLogEntity){
        operationLogValidator.validate(opLogEntity);
    }

}
