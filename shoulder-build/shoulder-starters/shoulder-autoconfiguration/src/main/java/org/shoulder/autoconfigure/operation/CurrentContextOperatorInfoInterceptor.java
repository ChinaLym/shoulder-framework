package org.shoulder.autoconfigure.operation;

import org.shoulder.core.context.AppContext;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.dto.ShoulderCurrentUserOperator;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志的拦截器：将当前单点登录的用户作为操作日志的默认操作者
 *
 * @author lym
 */
public class CurrentContextOperatorInfoInterceptor extends OperationLogOperatorInfoInterceptor {


    /**
     * 用当前登录的用户信息作为操作日志中操作者信息默认值
     * 也可覆盖，改为从 spring security context holder 中拿
     */
    @Override
    protected Operator resolveOperator(HttpServletRequest request) {
        ShoulderCurrentUserOperator operator = new ShoulderCurrentUserOperator(AppContext.getUserId());
        operator.setLanguageId(String.valueOf(AppContext.getLocale()));
        return operator;
    }
}
